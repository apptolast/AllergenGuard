import AuthenticationServices
import CryptoKit
import UIKit
import ConsumerApp

/// Drives the native Sign in with Apple flow and bridges it to Kotlin (`IosAppleAuthBridge`).
///
/// The Firebase exchange is NOT done here — Kotlin sends the returned identity token + raw nonce to
/// the shared Identity Toolkit `signInWithIdp` REST call, so this coordinator only needs the system
/// `AuthenticationServices` + `CryptoKit` frameworks (no Firebase/GoogleSignIn SPM packages).
final class SocialAuthCoordinator: NSObject {
    static let shared = SocialAuthCoordinator()

    private var appleCompletion: ((String?) -> Void)?
    private var currentNonce: String?
    // Retain the controller for the flow's lifetime; iOS can otherwise drop the delegate early.
    private var currentController: ASAuthorizationController?

    /// Installs the Kotlin bridge. Call once at launch (see `iOSApp.init`).
    func registerBridges() {
        IosAppleAuthBridge.shared.signInHandler = { [weak self] completion in
            self?.signInWithApple { payload in
                completion(payload)
            }
        }
    }

    private func signInWithApple(completion: @escaping (String?) -> Void) {
        DispatchQueue.main.async {
            let nonce = Self.randomNonceString()
            self.currentNonce = nonce
            self.appleCompletion = completion

            let request = ASAuthorizationAppleIDProvider().createRequest()
            request.requestedScopes = [.fullName, .email]
            // Apple signs the request with SHA256(nonce); the raw nonce is sent to Firebase, which
            // re-hashes it and compares against the token's `nonce` claim (replay protection).
            request.nonce = Self.sha256(nonce)

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            self.currentController = controller
            controller.performRequests()
        }
    }

    private func finish(_ payload: String?) {
        let completion = appleCompletion
        appleCompletion = nil
        currentController = nil
        currentNonce = nil
        completion?(payload)
    }

    // MARK: - Nonce helpers

    private static func randomNonceString(length: Int = 32) -> String {
        let charset: [Character] =
            Array("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remaining = length
        while remaining > 0 {
            var randoms = [UInt8](repeating: 0, count: 16)
            let status = SecRandomCopyBytes(kSecRandomDefault, randoms.count, &randoms)
            if status != errSecSuccess {
                fatalError("Unable to generate nonce. SecRandomCopyBytes failed with \(status)")
            }
            for random in randoms {
                if remaining == 0 {
                    break
                }
                if random < charset.count {
                    result.append(charset[Int(random)])
                    remaining -= 1
                }
            }
        }
        return result
    }

    private static func sha256(_ input: String) -> String {
        let hashed = SHA256.hash(data: Data(input.utf8))
        return hashed.map {
            String(format: "%02x", $0)
        }
        .joined()
    }
}

extension SocialAuthCoordinator: ASAuthorizationControllerDelegate {
    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard
            let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = credential.identityToken,
            let idToken = String(data: tokenData, encoding: .utf8),
            let rawNonce = currentNonce
        else {
            finish(nil)
            return
        }
        let name = [credential.fullName?.givenName, credential.fullName?.familyName]
        .compactMap {
            $0
        }
        .joined(separator: " ")
        // "idToken|||rawNonce|||displayName" — parsed by IosSocialAuthClient.
        finish("\(idToken)|||\(rawNonce)|||\(name)")
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        // Cancel / error → nil payload → Kotlin treats it as a silent cancellation.
        finish(nil)
    }
}

extension SocialAuthCoordinator: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene
        return windowScene?.windows.first(where: { $0.isKeyWindow })
            ?? windowScene?.windows.first
            ?? ASPresentationAnchor()
    }
}
