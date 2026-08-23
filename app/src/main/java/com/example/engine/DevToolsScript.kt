package com.example.engine

object DevToolsScript {

    val INJECTION_AGENT_JS: String = """
        (function() {
            if (window.__REQINSPECT_INITIALIZED__) return;
            window.__REQINSPECT_INITIALIZED__ = true;

            console.log('[ReqInspect] DevTools Inspector Agent Initialized');

            // --- 1. HOOK FETCH API ---
            const originalFetch = window.fetch;
            window.fetch = async function(...args) {
                const startTime = performance.now();
                const requestId = 'req_' + Math.random().toString(36).substr(2, 9) + '_' + Date.now();
                
                let resource = args[0];
                let init = args[1] || {};
                
                let url = '';
                let method = 'GET';
                let reqHeaders = {};
                let reqBody = '';

                if (typeof resource === 'string') {
                    url = resource;
                } else if (resource instanceof Request) {
                    url = resource.url;
                    method = resource.method || 'GET';
                    try {
                        for (let [k, v] of resource.headers.entries()) {
                            reqHeaders[k] = v;
                        }
                    } catch(e) {}
                }

                if (init.method) method = init.method.toUpperCase();
                
                if (init.headers) {
                    if (init.headers instanceof Headers) {
                        for (let [k, v] of init.headers.entries()) {
                            reqHeaders[k] = v;
                        }
                    } else if (Array.isArray(init.headers)) {
                        init.headers.forEach(([k, v]) => { reqHeaders[k] = v; });
                    } else if (typeof init.headers === 'object') {
                        Object.assign(reqHeaders, init.headers);
                    }
                }

                if (init.body) {
                    if (typeof init.body === 'string') {
                        reqBody = init.body;
                    } else if (init.body instanceof URLSearchParams) {
                        reqBody = init.body.toString();
                    } else if (init.body instanceof FormData) {
                        let entries = [];
                        for (let [k, v] of init.body.entries()) {
                            entries.push(k + '=' + (typeof v === 'string' ? v : '[File ' + (v.name || 'blob') + ']'));
                        }
                        reqBody = entries.join('&');
                    } else {
                        try {
                            reqBody = JSON.stringify(init.body);
                        } catch(e) {
                            reqBody = '[Complex Binary / Object Body]';
                        }
                    }
                }

                try {
                    const response = await originalFetch.apply(this, args);
                    const duration = Math.round(performance.now() - startTime);
                    const clone = response.clone();
                    
                    let resHeaders = {};
                    try {
                        for (let [k, v] of clone.headers.entries()) {
                            resHeaders[k] = v;
                        }
                    } catch(e) {}

                    let resBodyText = '';
                    let contentLength = 0;
                    try {
                        resBodyText = await clone.text();
                        contentLength = resBodyText.length;
                    } catch(e) {
                        resBodyText = '[Binary / Streamed Body]';
                    }

                    if (window.ReqInspectBridge && window.ReqInspectBridge.onFetchEvent) {
                        window.ReqInspectBridge.onFetchEvent(
                            url,
                            method,
                            response.status,
                            response.statusText || 'OK',
                            'fetch',
                            JSON.stringify(reqHeaders),
                            JSON.stringify(resHeaders),
                            reqBody,
                            resBodyText.substring(0, 100000), // Cap at 100KB for preview safety
                            contentLength,
                            duration
                        );
                    }
                    return response;
                } catch(error) {
                    const duration = Math.round(performance.now() - startTime);
                    if (window.ReqInspectBridge && window.ReqInspectBridge.onFetchEvent) {
                        window.ReqInspectBridge.onFetchEvent(
                            url,
                            method,
                            0,
                            'Failed: ' + (error.message || 'Network Error'),
                            'fetch',
                            JSON.stringify(reqHeaders),
                            '{}',
                            reqBody,
                            '',
                            0,
                            duration
                        );
                    }
                    throw error;
                }
            };

            // --- 2. HOOK XMLHTTPREQUEST (XHR) ---
            const originalXHR = window.XMLHttpRequest;
            function ReqInspectXHR() {
                const xhr = new originalXHR();
                let _url = '';
                let _method = 'GET';
                let _reqHeaders = {};
                let _reqBody = '';
                let _startTime = 0;

                const origOpen = xhr.open;
                xhr.open = function(method, url) {
                    _method = (method || 'GET').toUpperCase();
                    _url = url;
                    return origOpen.apply(xhr, arguments);
                };

                const origSetRequestHeader = xhr.setRequestHeader;
                xhr.setRequestHeader = function(header, value) {
                    _reqHeaders[header] = value;
                    return origSetRequestHeader.apply(xhr, arguments);
                };

                const origSend = xhr.send;
                xhr.send = function(body) {
                    _startTime = performance.now();
                    if (body) {
                        if (typeof body === 'string') {
                            _reqBody = body;
                        } else {
                            try {
                                _reqBody = JSON.stringify(body);
                            } catch(e) {
                                _reqBody = '[Binary / FormData Body]';
                            }
                        }
                    }

                    xhr.addEventListener('loadend', function() {
                        const duration = Math.round(performance.now() - _startTime);
                        let resHeaders = {};
                        try {
                            const rawHeaders = xhr.getAllResponseHeaders();
                            if (rawHeaders) {
                                rawHeaders.trim().split(/[\r\n]+/).forEach(function(line) {
                                    const parts = line.split(': ');
                                    const header = parts.shift();
                                    const value = parts.join(': ');
                                    if (header) resHeaders[header] = value;
                                });
                            }
                        } catch(e) {}

                        let resBody = '';
                        try {
                            resBody = xhr.responseText || '';
                        } catch(e) {
                            resBody = '[Non-text response]';
                        }

                        if (window.ReqInspectBridge && window.ReqInspectBridge.onXhrEvent) {
                            window.ReqInspectBridge.onXhrEvent(
                                _url,
                                _method,
                                xhr.status,
                                xhr.statusText || (xhr.status === 200 ? 'OK' : 'Status ' + xhr.status),
                                'xhr',
                                JSON.stringify(_reqHeaders),
                                JSON.stringify(resHeaders),
                                _reqBody,
                                resBody.substring(0, 100000),
                                resBody.length,
                                duration
                            );
                        }
                    });

                    return origSend.apply(xhr, arguments);
                };

                return xhr;
            }
            window.XMLHttpRequest = ReqInspectXHR;

            // --- 3. HOOK CONSOLE LOGS ---
            const origLog = console.log;
            const origInfo = console.info;
            const origWarn = console.warn;
            const origError = console.error;

            function formatArgs(args) {
                return Array.from(args).map(arg => {
                    if (typeof arg === 'object') {
                        try { return JSON.stringify(arg); } catch(e) { return String(arg); }
                    }
                    return String(arg);
                }).join(' ');
            }

            console.log = function(...args) {
                origLog.apply(console, args);
                if (window.ReqInspectBridge && window.ReqInspectBridge.onConsoleLog) {
                    window.ReqInspectBridge.onConsoleLog('log', formatArgs(args));
                }
            };
            console.info = function(...args) {
                origInfo.apply(console, args);
                if (window.ReqInspectBridge && window.ReqInspectBridge.onConsoleLog) {
                    window.ReqInspectBridge.onConsoleLog('info', formatArgs(args));
                }
            };
            console.warn = function(...args) {
                origWarn.apply(console, args);
                if (window.ReqInspectBridge && window.ReqInspectBridge.onConsoleLog) {
                    window.ReqInspectBridge.onConsoleLog('warn', formatArgs(args));
                }
            };
            console.error = function(...args) {
                origError.apply(console, args);
                if (window.ReqInspectBridge && window.ReqInspectBridge.onConsoleLog) {
                    window.ReqInspectBridge.onConsoleLog('error', formatArgs(args));
                }
            };
        })();
    """.trimIndent()
}
