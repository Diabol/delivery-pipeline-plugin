(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
var internal = require("./internal");
var promise = require("./promise");
var onRegisterTimeout;

/**
 * Asynchronously import/require a set of modules.
 *
 * <p>
 * Responsible for triggering the async loading of modules from plugins if
 * a given module is not already loaded.
 *
 * @param moduleQNames... A list of module "qualified" names, each containing the module name prefixed with the Jenkins plugin name
 * separated by a colon i.e. "<pluginName>:<moduleName>" e.g. "jquery:jquery2".
 *
 * @return A Promise, allowing async load of all modules. The promise is only fulfilled when all modules are loaded.
 */
exports.import = function() {
    if (arguments.length === 1) {
        return internal.import(arguments[0], onRegisterTimeout);        
    }
    
    var moduleQNames = [];    
    for (var i = 0; i < arguments.length; i++) {
        var argument = arguments[i];
        if (typeof argument === 'string') {
            moduleQNames.push(argument);
        }
    }
    
    if (moduleQNames.length == 0) {
        throw "No plugin module names specified.";
    }
    
    return promise.make(function (resolve, reject) {
        var fulfillments = [];
        
        function onFulfillment() {
            if (fulfillments.length === moduleQNames.length) {
                var modules = [];
                for (var i = 0; i < fulfillments.length; i++) {
                    if (fulfillments[i].value) {
                        modules.push(fulfillments[i].value);
                    } else {
                        // don't have everything yet so can't fulfill all.
                        return;
                    }
                }
                // If we make it here, then we have fulfilled all individual promises, which 
                // means we can now fulfill the top level import promise.
                resolve(modules);
            }
        }        
        
        // doRequire for each module
        for (var i = 0; i < moduleQNames.length; i++) {           
            function doRequire(moduleQName) {
                var promise = internal.import(moduleQName, onRegisterTimeout);
                var fulfillment = {
                    promise: promise,
                    value: undefined
                };
                fulfillments.push(fulfillment);
                promise
                    .onFulfilled(function(value) {
                        fulfillment.value = value;
                        onFulfillment();
                    })
                    .onRejected(function(error) {
                        reject(error);
                    });
            }
            doRequire(moduleQNames[i]);
        }
    }).applyArgsOnFulfill();    
};

/**
 * Synchronously "require" a module that it already loaded/registered.
 *
 * <p>
 * This function will throw an error if the module is not already loaded via an outer call to 'import'
 * (or 'import').
 *
 * @param moduleQName The module "qualified" name containing the module name prefixed with the Jenkins plugin name
 * separated by a colon i.e. "<pluginName>:<moduleName>" e.g. "jquery:jquery2".
 *
 * @return The module.
 */
exports.require = function(moduleQName) {
    var parsedModuleName = internal.parseModuleQName(moduleQName);
    var module = internal.getModule(parsedModuleName);    
    if (!module) {
        throw "Unable to perform synchronous 'require' for module '" + moduleQName + "'. This module is not pre-loaded. " +
            "The module needs to have been asynchronously pre-loaded via an outer call to 'import'.";
    }
    return module.exports;
}

/**
 * Export a module.
 * 
 * @param pluginName The Jenkins plugin in which the module resides, or "undefined" if the modules is in
 * the "global" module namespace e.g. a Jenkins core bundle.
 * @param moduleName The name of the module. 
 * @param module The CommonJS style module, or "undefined" if we just want to notify other modules waiting on
 * the loading of this module.
 * @param onError On error callback;
 */
exports.export = function(pluginName, moduleName, module, onError) {
    internal.onReady(function() {
        try {
            var moduleSpec = {pluginName: pluginName, moduleName: moduleName};
            var moduleNamespace = internal.getModuleNamespace(moduleSpec);
            
            if (moduleNamespace[moduleName]) {
                if (pluginName) {
                    throw "Jenkins plugin module '" + pluginName + ":" + moduleName + "' already registered.";
                } else {
                    throw "Jenkins global module '" + moduleName + "' already registered.";
                }
            }
            
            if (!module) {
                module = {
                    exports: {}
                };
            } else if (module.exports === undefined) {
                module = {
                    exports: module
                };
            }
            moduleNamespace[moduleName] = module;
            
            // Notify all that the module has been registered. See internal.loadModule also.
            internal.notifyModuleExported(moduleSpec, module.exports);
        } catch (e) {
            console.error(e);
            if (onError) {
                onError(e);
            }
        }
    });
};

/**
 * Add a module's CSS to the browser page.
 * 
 * <p>
 * The assumption is that the CSS can be accessed at
 * {@code <rootURL>/plugin/<pluginName>/jsmodules/<moduleName>/style.css}
 * 
 * @param pluginName The Jenkins plugin in which the module resides.
 * @param moduleName The name of the module. 
 * @param onError On error callback;
 */
exports.addModuleCSSToPage = function(pluginName, moduleName, onError) {
    internal.onReady(function() {
        try {
            internal.addModuleCSSToPage(pluginName, moduleName);
        } catch (e) {
            console.error(e);
            if (onError) {
                onError(e);
            }
        }
    });
};

/**
 * Add a plugin CSS file to the browser page.
 * 
 * @param pluginName The Jenkins plugin in which the module resides.
 * @param moduleName The name of the module. 
 * @param onError On error callback;
 */
exports.addPluginCSSToPage = function(pluginName, cssPath, onError) {
    internal.onReady(function() {
        try {
            internal.addPluginCSSToPage(pluginName, cssPath);
        } catch (e) {
            console.error(e);
            if (onError) {
                onError(e);
            }
        }
    });
};

/**
 * Add a javascript &lt;script&gt; element to the document &lt;head&gt;.
 * <p/>
 * Options:
 * <ul>
 *     <li><strong>scriptId</strong>: The script Id to use for the element. If not specified, one will be generated from the scriptSrc.</li>
 *     <li><strong>async</strong>: Asynchronous loading of the script. Default is 'true'.</li>
 *     <li><strong>success</strong>: An optional onload success function for the script element.</li>
 *     <li><strong>error</strong>: An optional onload error function for the script element. This is called if the .js file exists but there's an error evaluating the script. It is NOT called if the .js file doesn't exist (ala 404).</li>
 *     <li><strong>removeElementOnLoad</strong>: Remove the script element after loading the script. Default is 'false'.</li>
 * </ul>
 * 
 * @param scriptSrc The script src.
 * @param options Optional script load options object. See above.
 */
exports.addScript = function(scriptSrc, options) {
    internal.onReady(function() {
        internal.addScript(scriptSrc, options);
    });    
};

/**
 * Set the module registration timeout i.e. the length of time to wait for a module to load before failing.
 *
 * @param timeout Millisecond duration before onRegister times out. Defaults to 10000 (10s) if not specified.
 */
exports.setRegisterTimeout = function(timeout) {
    onRegisterTimeout = timeout;
}

/**
 * Set the Jenkins root/base URL.
 * 
 * @param rootUrl The root/base URL.
 */
exports.setRootURL = function(rootUrl) {
    internal.setRootURL(rootUrl);
};

/**
 * Manually initialise the Jenkins Global.
 * <p>
 * This should only ever be called from a test environment.
 */
exports.initJenkinsGlobal = function() {
    internal.initJenkinsGlobal();
};

internal.onJenkinsGlobalInit(function(jenkinsCIGlobal) {
    // For backward compatibility, we need to make some jenkins-js-modules
    // functions globally available e.g. to allow legacy code wait for
    // certain modules to be loaded, as with legacy adjuncts.
    if (!jenkinsCIGlobal._internal) {
        // Put the functions on an object called '_internal' as a way
        // of hinting to people to not use it.
        jenkinsCIGlobal._internal = {
            import: exports.import,
            addScript: internal.addScript
        };
    }
});
},{"./internal":2,"./promise":3}],2:[function(require,module,exports){
var promise = require("./promise");
var windowHandle = require("window-handle");
var jenkinsCIGlobal;
var globalInitListeners = [];

exports.onReady = function(callback) {
    // This allows test based initialization of jenkins-js-modules when there might 
    // not yet be a global window object.
    if (jenkinsCIGlobal) {
        callback();
    } else {
        windowHandle.getWindow(function() {
            callback();
        });
    }    
};

exports.onJenkinsGlobalInit = function(callback) {
    globalInitListeners.push(callback);
};

exports.initJenkinsGlobal = function() {
    jenkinsCIGlobal = {
    };
    if (globalInitListeners) {
        for (var i = 0; i < globalInitListeners.length; i++) {
            globalInitListeners[i](jenkinsCIGlobal);
        }
    }
};

exports.clearJenkinsGlobal = function() {    
    jenkinsCIGlobal = undefined;
};

exports.getJenkins = function() {
    if (jenkinsCIGlobal) {
        return jenkinsCIGlobal;
    }
    var window = windowHandle.getWindow();
    if (window.jenkinsCIGlobal) {
        jenkinsCIGlobal = window.jenkinsCIGlobal;
    } else {
        exports.initJenkinsGlobal();
        jenkinsCIGlobal.rootURL = getRootURL();
        window.jenkinsCIGlobal = jenkinsCIGlobal;
    }   
    return jenkinsCIGlobal;
};

exports.getModuleNamespace = function(moduleSpec) {
    if (moduleSpec.pluginName) {
        return exports.getPlugin(moduleSpec.pluginName);
    } else {
        return exports.getGlobalModules();
    }
}

exports.getPlugin = function(pluginName) {
    var plugins = exports.getPlugins();
    var pluginNamespace = plugins[pluginName];
    if (!pluginNamespace) {
        pluginNamespace = {
            globalNS: false            
        };
        plugins[pluginName] = pluginNamespace;
    }
    return pluginNamespace;
};

exports.import = function(moduleQName, onRegisterTimeout) {
    return promise.make(function (resolve, reject) {
        // getPlugin etc needs to access the 'window' global. We want to make sure that
        // exists before attempting to fulfill the require operation. It may not exists
        // immediately in a test env.
        exports.onReady(function() {
            var moduleSpec = exports.parseModuleQName(moduleQName);
            var module = exports.getModule(moduleSpec);
            
            if (module) {
                // module already loaded
                resolve(module.exports);
            } else {
                if (onRegisterTimeout === 0) {
                    if (moduleSpec.pluginName) {
                        throw 'Plugin module ' + moduleSpec.pluginName + ':' + moduleSpec.moduleName + ' require failure. Async load mode disabled.';
                    } else {
                        throw 'Global module ' + moduleSpec.moduleName + ' require failure. Async load mode disabled.';
                    }
                }

                // module not loaded. Load async, fulfilling promise once registered
                exports.loadModule(moduleSpec, onRegisterTimeout)
                    .onFulfilled(function (moduleExports) {
                        resolve(moduleExports);
                    })
                    .onRejected(function (error) {
                        reject(error);
                    });
            }
        });
    });    
};

exports.loadModule = function(moduleSpec, onRegisterTimeout) {
    var moduleNamespace = exports.getModuleNamespace(moduleSpec);
    var module = moduleNamespace[moduleSpec.moduleName];
    
    if (module) {
        // Module already loaded. This prob shouldn't happen.
        console.log("Unexpected call to 'loadModule' for a module (" + moduleSpec.moduleName + ") that's already loaded.");
        return promise.make(function (resolve) {
            resolve(module.exports);
        });
    }

    function waitForRegistration(loadingModule, onRegisterTimeout) {
        return promise.make(function (resolve, reject) {
            if (typeof onRegisterTimeout !== "number") {
                onRegisterTimeout = 10000;
            }
            
            var timeoutObj = setTimeout(function () {
                // Timed out waiting on the module to load and register itself.
                if (!loadingModule.loaded) {
                    var moduleSpec = loadingModule.moduleSpec;
                    var errorDetail;
                    
                    if (moduleSpec.pluginName) {
                        errorDetail = "Please verify that the plugin '" +
                            moduleSpec.pluginName + "' is installed, and that " +
                            "it registers a module named '" + moduleSpec.moduleName + "'";
                    } else {
                        errorDetail = "Timed out waiting on global module '" + moduleSpec.moduleName + "' to load.";
                    }                    
                    console.error('Module load failure: ' + errorDetail);

                    // Call the reject function and tell it we timed out
                    reject({
                        reason: 'timeout',
                        detail: errorDetail
                    });
                }
            }, onRegisterTimeout);
            
            loadingModule.waitList.push({
                resolve: resolve,
                timeoutObj: timeoutObj
            });                    
        });
    }
    
    var loadingModule = getLoadingModule(moduleNamespace, moduleSpec.moduleName);
    if (!loadingModule.waitList) {
        loadingModule.waitList = [];
    }
    loadingModule.moduleSpec = moduleSpec; 
    loadingModule.loaded = false;

    try {
        return waitForRegistration(loadingModule, onRegisterTimeout);
    } finally {
        // We can auto/dynamic load modules in a plugin namespace. Global namespace modules
        // need to make sure they load themselves (via an adjunct, or whatever).
        if (moduleSpec.pluginName) {
            var scriptId = exports.toPluginModuleId(moduleSpec.pluginName, moduleSpec.moduleName) + ':js';
            var scriptSrc = exports.toPluginModuleSrc(moduleSpec.pluginName, moduleSpec.moduleName);
            exports.addScript(scriptSrc, {
                scriptId: scriptId,
                scriptSrcBase: ''
            });
        }
    }
};

exports.addScript = function(scriptSrc, options) {
    if (!scriptSrc) {
        console.warn('Call to addScript with undefined "scriptSrc" arg.');
        return;
    }    
    
    var normalizedOptions;
    
    // If there's no options object, create it.
    if (typeof options === 'object') {
        normalizedOptions = options;
    } else {
        normalizedOptions = {};
    }
    
    // May want to transform/map some urls.
    if (normalizedOptions.scriptSrcMap) {
        if (typeof normalizedOptions.scriptSrcMap === 'function') {
            scriptSrc = normalizedOptions.scriptSrcMap(scriptSrc);
        } else if (Array.isArray(normalizedOptions.scriptSrcMap)) {
            // it's an array of suffix mappings
            for (var i = 0; i < normalizedOptions.scriptSrcMap.length; i++) {
                var mapping = normalizedOptions.scriptSrcMap[i];
                if (mapping.from && mapping.to) {
                    if (endsWith(scriptSrc, mapping.from)) {
                        normalizedOptions.originalScriptSrc = scriptSrc;
                        scriptSrc = scriptSrc.replace(mapping.from, mapping.to);
                        break;
                    }
                }
            }
        }
    }
    
    normalizedOptions.scriptId = getScriptId(scriptSrc, options);
    
    // set some default options
    if (normalizedOptions.async === undefined) {
        normalizedOptions.async = true;
    }
    if (normalizedOptions.scriptSrcBase === undefined) {
        normalizedOptions.scriptSrcBase = getRootURL() + '/';
    }

    var document = windowHandle.getWindow().document;
    var head = exports.getHeadElement();
    var script = document.getElementById(normalizedOptions.scriptId);

    if (script) {
        var replaceable = script.getAttribute('data-replaceable');
        if (replaceable && replaceable === 'true') {
            // This <script> element is replaceable. In this case, 
            // we remove the existing script element and add a new one of the
            // same id and with the specified src attribute.
            // Adding happens below.
            script.parentNode.removeChild(script);
        } else {
            return undefined;
        }
    }

    script = createElement('script');

    // Parts of the following onload code were inspired by how the ACE editor does it,
    // as well as from the follow SO post: http://stackoverflow.com/a/4845802/1166986
    var onload = function (_, isAborted) {
        script.setAttribute('data-onload-complete', true);
        try {
            if (isAborted) {
                console.warn('Script load aborted: ' + scriptSrc);
            } else if (!script.readyState || script.readyState === "loaded" || script.readyState === "complete") {
                // If the options contains an onload function, call it.
                if (typeof normalizedOptions.success === 'function') {
                    normalizedOptions.success(script);
                }
                return;
            }
            if (typeof normalizedOptions.error === 'function') {
                normalizedOptions.error(script, isAborted);
            }
        } finally {
            if (normalizedOptions.removeElementOnLoad) {
                head.removeChild(script);
            }
            // Handle memory leak in IE
            script = script.onload = script.onreadystatechange = null;
        }
    };
    script.onload = onload; 
    script.onreadystatechange = onload;

    script.setAttribute('id', normalizedOptions.scriptId);
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', normalizedOptions.scriptSrcBase + scriptSrc);
    if (normalizedOptions.originalScriptSrc) {
        script.setAttribute('data-referrer', normalizedOptions.originalScriptSrc);        
    }
    if (normalizedOptions.async) {
        script.setAttribute('async', normalizedOptions.async);
    }
    
    head.appendChild(script);
    
    return script;
};

exports.notifyModuleExported = function(moduleSpec, moduleExports) {
    var moduleNamespace = exports.getModuleNamespace(moduleSpec);
    var loadingModule = getLoadingModule(moduleNamespace, moduleSpec.moduleName);
    
    loadingModule.loaded = true;
    if (loadingModule.waitList) {
        for (var i = 0; i < loadingModule.waitList.length; i++) {
            var waiter = loadingModule.waitList[i];
            clearTimeout(waiter.timeoutObj);
            waiter.resolve(moduleExports);
        }
    }    
};

exports.addModuleCSSToPage = function(pluginName, moduleName) {
    var cssElId = exports.toPluginModuleId(pluginName, moduleName) + ':css';
    exports.addPluginCSSToPage(pluginName, 'jsmodules/' + moduleName + '/style.css', cssElId);
};

exports.addPluginCSSToPage = function(pluginName, cssPath, cssElId) {
    var document = windowHandle.getWindow().document;
    
    if (!cssElId) {
        cssElId = 'jenkins-plugin:' + pluginName + ':' + ':css:' + cssPath;
    }
    
    var cssEl = document.getElementById(cssElId);
    
    if (cssEl) {
        // already added to page
        return;
    }

    var cssPath = exports.getPluginPath(pluginName) + '/' + cssPath;
    var docHead = exports.getHeadElement();
    cssEl = createElement('link');
    cssEl.setAttribute('id', cssElId);
    cssEl.setAttribute('type', 'text/css');
    cssEl.setAttribute('rel', 'stylesheet');
    cssEl.setAttribute('href', cssPath);
    docHead.appendChild(cssEl);
};

exports.getGlobalModules = function() {
    var jenkinsCIGlobal = exports.getJenkins();
    if (!jenkinsCIGlobal.globals) {
        jenkinsCIGlobal.globals = {
            globalNS: true
        };
    }
    return jenkinsCIGlobal.globals;
};

exports.getPlugins = function() {
    var jenkinsCIGlobal = exports.getJenkins();
    if (!jenkinsCIGlobal.plugins) {
        jenkinsCIGlobal.plugins = {};
    }
    return jenkinsCIGlobal.plugins;
};

exports.toPluginModuleId = function(pluginName, moduleName) {
    return 'jenkins-plugin-module:' + pluginName + ':' + moduleName;
};

exports.toPluginModuleSrc = function(pluginName, moduleName) {
    return exports.getJSModulesDir(pluginName) + '/' + moduleName + '.js';
};

exports.getJSModulesDir = function(pluginName) {
    return exports.getPluginPath(pluginName) + '/jsmodules';
};

exports.getPluginPath = function(pluginName) {
    return getRootURL() + '/plugin/' + pluginName;
};

exports.getHeadElement = function() {
    var window = windowHandle.getWindow();
    var docHead = window.document.getElementsByTagName("head");
    if (!docHead || docHead.length == 0) {
        throw 'No head element found in document.';
    }
    return docHead[0];
};

exports.setRootURL = function(url) {    
    if (!jenkinsCIGlobal) {
        exports.initJenkinsGlobal();
    }
    jenkinsCIGlobal.rootURL = url;
};

exports.parseModuleQName = function(moduleQName) {
    var qNameTokens = moduleQName.split(":");    
    if (qNameTokens.length === 2) {
        return {
            pluginName: qNameTokens[0].trim(),
            moduleName: qNameTokens[1].trim()
        };
    } else {
        // The module/bundle is not in a plugin and doesn't
        // need to be loaded i.e. it will load itself and export.
        return {
            moduleName: qNameTokens[0].trim()
        };
    }
}

exports.getModule = function(moduleSpec) {
    if (moduleSpec.pluginName) {
        var plugin = exports.getPlugin(moduleSpec.pluginName);
        return plugin[moduleSpec.moduleName];
    } else {
        var globals = exports.getGlobalModules();
        return globals[moduleSpec.moduleName];
    }
}

function getScriptId(scriptSrc, config) {
    if (typeof config === 'string') {
        return config;
    } else if (typeof config === 'object' && config.scriptId) {
        return config.scriptId;
    } else {
        return 'jenkins-script:' + scriptSrc;
    }    
}

function getRootURL() {
    if (jenkinsCIGlobal && jenkinsCIGlobal.rootURL) {
        return jenkinsCIGlobal.rootURL;
    }
    
    var docHead = exports.getHeadElement();
    var resURL = getAttribute(docHead, "resURL");

    if (!resURL) {
        throw "Attribute 'resURL' not defined on the document <head> element.";
    }

    if (jenkinsCIGlobal) {
        jenkinsCIGlobal.rootURL = resURL;
    }
    
    return resURL;
}

function createElement(name) {
    var document = windowHandle.getWindow().document;
    return document.createElement(name);
}

function getAttribute(element, attributeName) {
    var value = element.getAttribute(attributeName.toLowerCase());
    
    if (value) {
        return value;
    } else {
        // try without lowercasing
        return element.getAttribute(attributeName);
    }    
}

function getLoadingModule(moduleNamespace, moduleName) {
    if (!moduleNamespace.loadingModules) {
        moduleNamespace.loadingModules = {};
    }
    if (!moduleNamespace.loadingModules[moduleName]) {
        moduleNamespace.loadingModules[moduleName] = {};
    }
    return moduleNamespace.loadingModules[moduleName];
}

function endsWith(string, suffix) {
    return (string.indexOf(suffix, string.length - suffix.length) !== -1);
}

},{"./promise":3,"window-handle":4}],3:[function(require,module,exports){
/*
 * Very simple "Promise" impl.
 * <p>
 * Intentionally not using the "promise" module/polyfill because it will add a few Kb and we 
 * only need something very simple here. We really just want to follow the main pattern
 * and don't need some of the fancy stuff.
 * <p>
 * I think so long as we stick to same interface/interaction pattern as outlined in the link
 * below, then we can always switch to the "promise" module later without breaking anything.
 * <p>
 * See https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Promise
 */

exports.make = function(executor) {
    var thePromise = new APromise();
    executor.call(thePromise, function(result) {
        thePromise.resolve(result);
    }, function(reason) {
        thePromise.reject(reason);
    });
    return thePromise;
};

function APromise() {
    this.state = 'PENDING';
    this.whenFulfilled = undefined;
    this.whenRejected = undefined;
    this.applyFulfillArgs = false;
}

APromise.prototype.applyArgsOnFulfill = function() {
    this.applyFulfillArgs = true;
    return this;
}

APromise.prototype.resolve = function (result) {
    this.state = 'FULFILLED';
    
    var thePromise = this;
    function doFulfill(whenFulfilled, result) {
        if (thePromise.applyFulfillArgs) {
            whenFulfilled.apply(whenFulfilled, result);
        } else {
            whenFulfilled(result);
        }
    }
    
    if (this.whenFulfilled) {
        doFulfill(this.whenFulfilled, result);
    }
    // redefine "onFulfilled" to call immediately
    this.onFulfilled = function (whenFulfilled) {
        if (whenFulfilled) {
            doFulfill(whenFulfilled, result);
        }
        return this;
    }
};

APromise.prototype.reject = function (reason) {
    this.state = 'REJECTED';
    if (this.whenRejected) {
        this.whenRejected(reason);
    }
    // redefine "onRejected" to call immediately
    this.onRejected = function(whenRejected) {
        if (whenRejected) {
            whenRejected(reason);
        }
        return this;
    }
};

APromise.prototype.onFulfilled = function(whenFulfilled) {
    if (!whenFulfilled) {
        throw 'Must provide an "whenFulfilled" callback.';
    }
    this.whenFulfilled = whenFulfilled;
    return this;
};

APromise.prototype.onRejected = function(whenRejected) {        
    if (whenRejected) {
        this.whenRejected = whenRejected;
    }
    return this;
};

},{}],4:[function(require,module,exports){
var theWindow;
var defaultTimeout = 10000;
var callbacks = [];
var windowSetTimeouts = [];

function execCallback(callback, theWindow) {
    if (callback) {
        try {
            callback.call(callback, theWindow);                
        } catch (e) {
            console.log("Error invoking window-handle callback.");
            console.log(e);
        }
    }
}

/**
 * Get the global "window" object.
 * @param callback An optional callback that can be used to receive the window asynchronously. Useful when
 * executing in test environment i.e. where the global window object might not exist immediately. 
 * @param timeout The timeout if waiting on the global window to be initialised.
 * @returns {*}
 */
exports.getWindow = function(callback, timeout) {
    
	if (theWindow) {
        execCallback(callback, theWindow);
        return theWindow;
	} 
	
	try {
		if (window) {
            execCallback(callback, window);
			return window;
		} 
	} catch (e) {
		// no window "yet". This should only ever be the case in a test env.
		// Fall through and use callbacks, if supplied.
	}

	if (callback) {
        function waitForWindow(callback) {
            callbacks.push(callback);
            var windowSetTimeout = setTimeout(function() {
                callback.error = "Timed out waiting on the window to be set.";
                callback.call(callback);
            }, (timeout?timeout:defaultTimeout));
            windowSetTimeouts.push(windowSetTimeout);
        }
        waitForWindow(callback);
	} else {
		throw "No 'window' available. Consider providing a 'callback' and receiving the 'window' async when available. Typically, this should only be the case in a test environment.";
	}
}

/**
 * Set the global window e.g. in a test environment.
 * <p>
 * Once called, all callbacks (registered by earlier 'getWindow' calls) will be invoked.
 * 
 * @param newWindow The window.
 */
exports.setWindow = function(newWindow) {
	for (var i = 0; i < windowSetTimeouts.length; i++) {
		clearTimeout(windowSetTimeouts[i]);
	}
    windowSetTimeouts = [];
	theWindow = newWindow;
	for (var i = 0; i < callbacks.length; i++) {
		execCallback(callbacks[i], theWindow);
	}
    callbacks = [];
}

/**
 * Set the default time to wait for the global window to be set.
 * <p>
 * Default is 10 seconds (10000 ms).
 * 
 * @param millis Milliseconds to wait for the global window to be set.
 */
exports.setDefaultTimeout = function(millis) {
    defaultTimeout = millis;
}
},{}],5:[function(require,module,exports){
var exporter = require('./util/exporter');

exporter.exportACE('ace-editor-119');
		require('jenkins-js-modules').export(undefined, 'ace-editor-119', {});
},{"./util/exporter":7,"jenkins-js-modules":1}],6:[function(require,module,exports){
var queue = [];
var inProcess = false;
var windowHandle = require('window-handle');

exports.add = function(queueEntry) {
    queue.push(queueEntry);
    
    if (!inProcess) {
        inProcess = true;
        processQueue();
    }
};

function processQueue() {
    var window = windowHandle.getWindow();
    var hasActiveScripts = hasActiveACEScripts();
    
    // If there's nothing in the queue and there are no active ACE editor
    // scripts (i.e. not fully loaded - the elements get removed after loading)
    // then we are done processing the queue for now. It will get fired
    // again next time something is added to the queue.
    if (queue.length === 0 && !hasActiveScripts) {
        // clear the ace object from the global namespace
        delete window.ace;
        inProcess = false;
        return;
    }    
    
    // Only process queue entries if there's nothing going on wrt loading
    // ACE scripts. If there are active scripts then fall through, set the
    // timer and come back in a while.
    if(queue.length > 0 && !hasActiveScripts) {
        var nextJob = queue[0];
        
        // And remove the first element from the array.
        queue.splice(0, 1);
        
        // Attach the ACE instance to the global scope. Yeah, this is horrible
        // but a fact of life - all of the ACE extensions etc expect it to be
        // there. We will remove it later once the config is completed i.e after
        // all the ace <script>s have loaded and been removed.
        window.ace = nextJob.ace;
        nextJob.configFunc.call(nextJob);
    }
    
    // Come back in a while and see can we do anything or clear the
    // ACE globals.
    setTimeout(function() {
        processQueue();
    }, 20);
}

function hasActiveACEScripts() {
    var scripts = getHeadElement().getElementsByTagName("script");
    for (var i = 0; i < scripts.length; i++) {
        // Is it an ace-editor script?
        if (isACEScript(scripts[i].getAttribute('src')) || isACEScript(scripts[i].getAttribute('data-referrer'))) {
            return true;
        }
    }    
    return false;
}

function isACEScript(url) {
    return (url && url.indexOf('plugin/ace-editor/packs/') !== -1);
}

function getHeadElement() {
    var window = windowHandle.getWindow();
    var docHead = window.document.getElementsByTagName("head");

    if (!docHead || docHead.length === 0) {
        throw 'No head element found in document.';
    }
    
    return docHead[0];
}

},{"window-handle":4}],7:[function(require,module,exports){
var windowHandle = require('window-handle');

exports.exportACE = function(acepack) {
    var acepackObj = new ACEPack(acepack);
    
    acepackObj.addScript('ace.js', function () {
        var window = windowHandle.getWindow();
        var jenkinsJSModules = require('jenkins-js-modules');
    
        acepackObj.setACE(window.ace);
        delete window.ace;
        // We only export the acepackObj after the ace instance
        // has been attached.
        jenkinsJSModules.export('ace-editor', acepack, acepackObj);
    });    
};

function ACEPack(acepack) {
    this.acepack = acepack;
    this.jsMapping = [];
}

ACEPack.prototype.setACE = function (ace) {
    var net = ace.require('ace/lib/net');
    var acePack = this;

    // We replace ACE's own loadScript function with our own so as to
    // track the load state via the data-onload-complete attribute.
    var jenkinsJSModules = require('jenkins-js-modules');
    net.loadScript = function(path, callback) {
        jenkinsJSModules.addScript(path, {
            scriptSrcBase: '',
            success: callback,
            removeElementOnLoad: true,
            scriptSrcMap: acePack.jsMapping
        });
    };
    
    this.ace = ace;
};

ACEPack.prototype.addPackOverride = function (packRes, override) {
    this.jsMapping.push({
            from: 'plugin/ace-editor/packs/' + this.acepack + '/' + packRes,
            to: 'plugin/ace-editor/' + override
        }
    );
};

ACEPack.prototype.addScript = function (scriptName, callback) {
    var jenkinsJSModules = require('jenkins-js-modules');
    jenkinsJSModules.addScript('plugin/ace-editor/packs/' + this.acepack + '/' + scriptName, {
        success: callback,
        removeElementOnLoad: true
    });
};

ACEPack.prototype.edit = function(element, configClosure) {
    require('./config-queue').add({
        acePack: this,
        ace: this.ace,
        editor: this.ace.edit(element),
        configFunc: configClosure
    });
};

},{"./config-queue":6,"jenkins-js-modules":1,"window-handle":4}]},{},[5]);
