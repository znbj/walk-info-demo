/** EasyWeb spa v3.1.7 date:2020-02-08 License By http://easyweb.vip */

layui.define(function (exports) {
    var router = {
        index: '/',
        lash: null,
        routers: {},
        init: function (options) {
            router.index = router.routerInfo(options.index).path.join('/');
            if (options.pop && typeof options.pop === 'function') {
                router.pop = options.pop;
            }
            if (options.notFound && typeof options.notFound === 'function') {
                router.notFound = options.notFound;
            }
            onhashchange();
            window.onhashchange = function () {
                onhashchange();
            };
            return this;
        },
        /* 注册路由 */
        reg: function (hash, handler) {
            if (hash) {
                if (!handler) {
                    handler = function () {
                    };
                }
                if (hash instanceof Array) {
                    for (var i in hash) {
                        this.reg.apply(this, [hash[i], handler]);
                    }
                } else if (typeof hash === 'string') {
                    hash = router.routerInfo(hash).path.join('/');
                    if (typeof handler === 'function') {
                        router.routers[hash] = handler;
                    } else if (typeof handler === 'string' && router[handler]) {
                        router.routers[hash] = router.routers[handler];
                    }
                }
            }
            return this;
        },
        /* 获取路由信息 */
        routerInfo: function (url) {
            url || (url = location.hash);
            var hash = url.replace(/^#+/g, '').replace(/\/+/g, '/');
            if (hash.indexOf('/') !== 0) {
                hash = '/' + hash;
            }
            return layui.router('#' + hash);
        },
        /* 刷新路由 */
        refresh: function (url) {
            onhashchange(url, true);
        },
        /* 跳转路由 */
        go: function (hash) {
            location.hash = '#' + router.routerInfo(hash).href;
        }
    };

    function onhashchange(url, refresh) {
        var routerInfo = router.routerInfo(url);
        router.lash = routerInfo.href;
        var hash = routerInfo.path.join('/');
        if (!hash || hash === '/') {
            hash = router.index;
            routerInfo = router.routerInfo(router.index);
        }
        router.pop && router.pop.call(this, routerInfo);
        if (router.routers[hash]) {
            routerInfo.refresh = refresh;
            router.routers[hash].call(this, routerInfo);
        } else if (router.notFound) {
            router.notFound.call(this, routerInfo);
        }
    }

    exports('layRouter', router);
});
