// Run the Kotlin/JS + Kotlin/Wasm browser tests under a headless Chrome that
// works in sandboxed / rootless CI containers (where the default ChromeHeadless
// fails to start with "cannot start"). Honours the CHROME_BIN env var.
config.set({
    browsers: ["ChromeHeadlessNoSandbox"],
    customLaunchers: {
        ChromeHeadlessNoSandbox: {
            base: "ChromeHeadless",
            flags: ["--no-sandbox", "--disable-gpu", "--disable-dev-shm-usage"],
        },
    },
});
