# ============================================
# Dockerfile for MenuAdmin (Kotlin Multiplatform - WASM)
# Multi-stage build for optimized production image
# ============================================

# Stage 1: Build the WASM application
FROM gradle:8.10-jdk21 AS builder

# Build argument for API URL (passed from CI/CD)
ARG API_BASE_URL
ENV API_BASE_URL=${API_BASE_URL}

# Firebase web config (public client identifiers) + feature flag, consumed by BuildKonfig in :shared.
ARG FIREBASE_API_KEY=AIzaSyCNV6mF3J2ruBQ0Mi7a0RTqm4Xi5wlqn88
ARG FIREBASE_PROJECT_ID=menusmati
ARG USE_FIRESTORE=true
ENV FIREBASE_API_KEY=${FIREBASE_API_KEY}
ENV FIREBASE_PROJECT_ID=${FIREBASE_PROJECT_ID}
ENV USE_FIRESTORE=${USE_FIRESTORE}

# EmailJS client config (PUBLIC key only — never the private/access key), consumed by BuildKonfig.
ARG EMAILJS_PUBLIC_KEY
ARG EMAILJS_SERVICE_ID
ARG EMAILJS_TEMPLATE_ID
ENV EMAILJS_PUBLIC_KEY=${EMAILJS_PUBLIC_KEY}
ENV EMAILJS_SERVICE_ID=${EMAILJS_SERVICE_ID}
ENV EMAILJS_TEMPLATE_ID=${EMAILJS_TEMPLATE_ID}

# Install libatomic1 for Node.js v25+ (required by Kotlin/WASM)
USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    libatomic1 \
    && rm -rf /var/lib/apt/lists/*
USER gradle

WORKDIR /app

# Configure JVM/Gradle memory
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m"

# Copy Gradle configuration files first (for better caching)
COPY --chown=gradle:gradle gradle/ gradle/
COPY --chown=gradle:gradle gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./

# Copy the shared module (data + domain) and the admin app module
COPY --chown=gradle:gradle shared/ shared/
COPY --chown=gradle:gradle adminApp/ adminApp/

# Make gradlew executable
RUN chmod +x gradlew

# Create local.properties with API base URL + Firebase config (consumed by BuildKonfig in :shared).
# The admin web has NO runtime override of the Firestore DB (unlike the consumer apps), so it is frozen
# at build time: pin production to the `(default)` database explicitly (this image is always production).
RUN printf 'API_BASE_URL=%s\nFIREBASE_API_KEY=%s\nFIREBASE_PROJECT_ID=%s\nUSE_FIRESTORE=%s\nFIRESTORE_DATABASE_ID=(default)\nEMAILJS_PUBLIC_KEY=%s\nEMAILJS_SERVICE_ID=%s\nEMAILJS_TEMPLATE_ID=%s\n' \
    "${API_BASE_URL}" "${FIREBASE_API_KEY}" "${FIREBASE_PROJECT_ID}" "${USE_FIRESTORE}" "${EMAILJS_PUBLIC_KEY}" "${EMAILJS_SERVICE_ID}" "${EMAILJS_TEMPLATE_ID}" > local.properties

# Upgrade Yarn lock files (required after dependency changes)
RUN ./gradlew kotlinUpgradeYarnLock kotlinWasmUpgradeYarnLock --no-daemon

# Build the WASM distribution
RUN ./gradlew :adminApp:wasmJsBrowserDistribution --no-daemon --stacktrace

# Stage 2: Serve with Nginx
FROM nginx:alpine

RUN apk add --no-cache tzdata

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Copy the built WASM application
COPY --from=builder /app/adminApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

# Create a simple health check endpoint
RUN echo "OK" > /usr/share/nginx/html/health

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
