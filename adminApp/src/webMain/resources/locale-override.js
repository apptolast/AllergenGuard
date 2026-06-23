// Runtime language override for Compose Multiplatform resources (web).
// Makes navigator.language(s) honor window.__customLocale so the app can switch ES/EN at runtime,
// independently of the browser locale. Loaded BEFORE adminApp.js (see index.html).
// Pattern from https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html
(function () {
    function override(prop, makeValue) {
        var desc = Object.getOwnPropertyDescriptor(Navigator.prototype, prop);
        if (!desc || !desc.get) return;
        Object.defineProperty(Navigator.prototype, prop, Object.assign({}, desc, {
            get: function () {
                if (window.__customLocale) return makeValue(window.__customLocale);
                return desc.get.apply(this);
            }
        }));
    }
    override("languages", function (l) { return [l]; });
    override("language", function (l) { return l; });
})();
