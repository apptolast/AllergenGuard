// Silence the benign webpack warning:
//   "Critical dependency: the request of a dependency is an expression"
// It comes from a dynamic require() inside a bundled dependency and does not affect the
// Wasm app (verified: the app compiles and serves correctly). Suppressing only the noise.
config.ignoreWarnings = (config.ignoreWarnings || []).concat([
    /Critical dependency: the request of a dependency is an expression/,
]);
