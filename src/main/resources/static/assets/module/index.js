/** EasyWeb spa v3.1.8 date:2020-05-04 License By http://easyweb.vip */

layui.define(['layer', 'element', 'setter', 'layRouter', 'admin'], function (exports) {
    var $ = layui.jquery;
    var layer = layui.layer;
    var element = layui.element;
    var setter = layui.setter;
    var layRouter = layui.layRouter;
    var admin = layui.admin;
    var headerDOM = '.layui-layout-admin>.layui-header';
    var sideDOM = '.layui-layout-admin>.layui-side>.layui-side-scroll';
    var bodyDOM = '.layui-layout-admin>.layui-body';
    var tabDOM = bodyDOM + '>.layui-tab';
    var titleDOM = bodyDOM + '>.layui-body-header';
    var tabFilter = 'admin-pagetabs';
    var navFilter = 'admin-side-nav';
    var mIsAddTab = false;  // 是否是添加Tab，添加Tab的时候切换不自动刷新
    var index = {mTabList: []};

    /** 注册路由 */
    index.regRouter = function (menus, format) {
        $.each(menus, function (i, data) {
            if (format) data = format(data);
            if (data.url && data.url.indexOf('#') === 0) {
                layRouter.reg(data.url, function (info) {
                    index.changeView($.extend(info, {name: data.name, iframe: data.iframe}));
                });
            }
            if (data.subMenus) index.regRouter(data.subMenus, format);
        });
    };

    /** 处理路由改变 */
    index.changeView = function (info, first) {
        var path = index.getHashPath(info);  // 组件地址
        var contentDom = bodyDOM + '>div[lay-id]';  // 主体部分dom
        if (setter.pageTabs) {  // 多标签模式
            var flag;  // 选项卡是否已添加
            $(tabDOM + '>.layui-tab-title>li').each(function () {
                if ($(this).attr('lay-id') === path) flag = true;
            });
            if (!flag) {  // 添加选项卡
                if (index.mTabList.length + 1 >= setter.maxTabNum) {
                    layer.msg('最多打开' + setter.maxTabNum + '个选项卡', {icon: 2, anim: 6});
                    return history.back();
                }
                mIsAddTab = true;
                element.tabAdd(tabFilter, {
                    id: path, title: '<span class="title">' + (info.name || '') + '</span>',
                    content: '<div lay-id="' + path + '" lay-url="' + info.href + '"></div>'
                });
                if (path !== layRouter.index) index.mTabList.push(info);  // 记录tab
                if (setter.cacheTab) admin.putTempData('indexTabs', index.mTabList);  // 缓存tab
            }
            contentDom = tabDOM + '>.layui-tab-content>.layui-tab-item>div[lay-id="' + path + '"]';
            var oldUrl = $(contentDom).attr('lay-url');
            if (info.href !== oldUrl) {  // 同一个hash参数不同
                layui.event.call(this, 'admin', 'destroy(' + path + ')');  // 页面卸载回调
                $(contentDom).attr('lay-url', info.href);
                flag = false;  // 同一个hash参数不同则刷新
                for (var i = 0; i < index.mTabList.length; i++) if (index.mTabList[i].href === oldUrl) index.mTabList[i] = info;
                if (setter.cacheTab) admin.putTempData('indexTabs', index.mTabList);  // 缓存tab
            }
            if (!flag || info.refresh) {
                if (info.refresh) layui.event.call(this, 'admin', 'destroy(' + path + ')');  // 页面卸载回调
                index.renderView(info, contentDom); // 渲染主体部分
            }
            if (!info.noChange && !info.refresh) element.tabChange(tabFilter, path);  // 切换到此tab
        } else {  // 单标签模式
            admin.activeNav(info.href, !first);
            if ($(contentDom).length === 0) {
                $(bodyDOM).html([
                    '<div class="layui-body-header">',
                    '   <span class="layui-body-header-title"></span>',
                    '   <span class="layui-breadcrumb pull-right" lay-filter="admin-body-breadcrumb" style="visibility: visible;"></span>',
                    '</div>',
                    '<div lay-id="' + path + '" lay-url="' + info.href + '"></div>'
                ].join(''));
            } else {
                layui.event.call(this, 'admin', 'destroy(' + $(contentDom).attr('lay-id') + ')');  // 页面卸载回调
                $(contentDom).attr('lay-id', path).attr('lay-url', info.href);
            }
            $('[lay-filter="admin-body-breadcrumb"]').html(index.getBreadcrumbHtml(path));
            index.mTabList.splice(0, index.mTabList.length);
            if (path === layRouter.index) {
                index.setTabTitle($(info.name).text() || $(sideDOM + ' [href="#/' + layRouter.index + '"]').text() || '主页');
            } else {
                index.mTabList.push(info);
                index.setTabTitle(info.name);
            }
            index.renderView(info, contentDom); // 渲染主体部分
            if (setter.cacheTab) admin.putTempData('indexTabs', index.mTabList);  // 缓存tab
        }
        if (admin.getPageWidth() <= 768) admin.flexible(true); // 移动端自动收起侧导航
        $('.layui-table-tips-c').trigger('click'); // 切换tab关闭表格内浮窗
    };

    /** 渲染主体部分 */
    index.renderView = function (info, contentDom, loadingDOM) {
        var $contentDom = $(contentDom);
        if (!loadingDOM) loadingDOM = $contentDom.parent();
        if (!info.iframe) {
            admin.showLoading({elem: loadingDOM, size: ''});
            admin.ajax({
                url: setter.viewPath + '/' + info.path.join('/') + setter.viewSuffix,
                data: info.search,
                dataType: 'html',
                success: function (res) {
                    admin.removeLoading(loadingDOM);
                    if (typeof res !== 'string') res = JSON.stringify(res);
                    if (res.indexOf('<tpl') === 0) {
                        // 模板里面有动态模板处理
                        var $html = $('<div>' + res + '</div>'), tplAll = {};
                        $html.find('script,[tpl-ignore]').each(function (i) {
                            var $this = $(this);
                            tplAll['temp_' + i] = $this[0].outerHTML;
                            $this.after('${temp_' + i + '}').remove();
                        });
                        res = admin.util.tpl($html.html(), info, setter.tplOpen, setter.tplClose);
                        for (var f in tplAll) res = res.replace('${' + f + '}', tplAll[f]);
                    }
                    $contentDom.html(res);
                    admin.renderPerm();  // 移除没有权限的元素
                    admin.renderTpl(contentDom + ' [ew-tpl]');
                }
            });
        } else {
            $contentDom.html([
                '<div class="admin-iframe" style="-webkit-overflow-scrolling: touch;">',
                '   <iframe src="', info.iframe, '" class="admin-iframe" frameborder="0"></iframe>',
                '</div>'
            ].join(''));
        }
    };

    /** 加载主页 */
    index.loadHome = function (data) {
        var cacheTabs = admin.getTempData('indexTabs');  // 获取缓存tab
        index.regRouter([data]);
        if (setter.pageTabs) {
            var info = layRouter.routerInfo(data.url);
            layRouter.index = index.getHashPath(info);
            index.changeView($.extend(info, {name: data.name, iframe: data.iframe, noChange: true}), true);
            if (data.loadSetting !== false && setter.cacheTab && cacheTabs) {  // 恢复缓存tab
                for (var i = 0; i < cacheTabs.length; i++) index.changeView($.extend(cacheTabs[i], {noChange: true}));
            }
        }
        admin.removeLoading(undefined, false);
        layRouter.init({index: data.url, notFound: setter.routerNotFound});  // 初始化路由
    };

    /** 打开tab */
    index.openNewTab = function (param) {
        index.regRouter([param]);
        layRouter.go(param.url);
    };

    /** 关闭tab */
    index.closeTab = function (hash) {
        element.tabDelete(tabFilter, index.getHashPath(hash));
    };

    /** 获取hash路径 */
    index.getHashPath = function (hash) {
        if (!hash || typeof hash === 'string') hash = layRouter.routerInfo(hash);
        return hash.path.join('/');
    };

    /** 跳转tab */
    index.go = function (hash) {
        layRouter.go(hash);
    };

    /** 清除tab记忆 */
    index.clearTabCache = function () {
        admin.putTempData('indexTabs', null);
    };

    /** 设置tab标题 */
    index.setTabTitle = function (title, hash) {
        if (setter.pageTabs) {
            if (!hash) hash = index.getHashPath();
            if (hash) $(tabDOM + '>.layui-tab-title>li[lay-id="' + hash + '"] .title').html(title || '');
        } else if (title) {
            $(titleDOM + '>.layui-body-header-title').html(title);
            $(titleDOM).addClass('show');
            $(headerDOM).css('box-shadow', '0 1px 0 0 rgba(0, 0, 0, .03)');
        } else {
            $(titleDOM).removeClass('show');
            $(headerDOM).css('box-shadow', '');
        }
    };

    /** 自定义tab标题 */
    index.setTabTitleHtml = function (html) {
        if (setter.pageTabs) return;
        if (!html) return $(titleDOM).removeClass('show');
        $(titleDOM).html(html);
        $(titleDOM).addClass('show');
    };

    /** 获取面包屑 */
    index.getBreadcrumb = function (hash) {
        if (!hash) hash = $(bodyDOM + '>div[lay-id]').attr('lay-id');
        var breadcrumb = [];
        var $href = $(sideDOM).find('[href="#/' + hash + '"]');
        if ($href.length > 0) breadcrumb.push($href.text().replace(/(^\s*)|(\s*$)/g, ''));
        while (true) {
            $href = $href.parent('dd').parent('dl').prev('a');
            if ($href.length === 0) break;
            breadcrumb.unshift($href.text().replace(/(^\s*)|(\s*$)/g, ''));
        }
        return breadcrumb;
    };

    /** 获取面包屑结构 */
    index.getBreadcrumbHtml = function (hash) {
        var breadcrumb = index.getBreadcrumb(hash);
        var htmlStr = hash === layRouter.index ? '' : ('<a href="#/' + layRouter.index + '">首页</a>');
        for (var i = 0; i < breadcrumb.length - 1; i++) {
            if (htmlStr) htmlStr += '<span lay-separator="">/</span>';
            htmlStr += ('<a><cite>' + breadcrumb[i] + '</cite></a>');
        }
        return htmlStr;
    };

    /** 渲染侧边栏 */
    index.renderSide = function (data, tpl, callback, tplOpen, tplClose, format) {
        if (typeof tpl === 'function') {
            tplClose = tplOpen;
            tplOpen = callback;
            callback = tpl;
            tpl = undefined;
        }
        if ('Array' !== admin.util.isClass(data)) {
            tpl = data.tpl;
            callback = data.callback;
            tplOpen = data.tplOpen;
            tplClose = data.tplClose;
            format = data.format;
            data = data.data;
        }

        function removeHide(menus) {
            for (var i = menus.length - 1; i >= 0; i--) {
                if (format) menus[i] = format(menus[i]);
                if (menus[i].subMenus) removeHide(menus[i].subMenus);
                if (menus[i].show === false) {
                    menus.splice(i, 1);
                } else {
                    if (!menus[i].target) menus[i].target = '_self';
                    if (!menus[i].url) menus[i].url = 'javascript:;';
                }
            }
        }

        removeHide(data);  // 处理数据
        var html = admin.util.tpl(tpl || $('#sideNav').html() || '', data,
            tplOpen || setter.tplOpen, tplClose || setter.tplClose);
        if (callback) return callback(html, {
            data: data, side: sideDOM, render: function () {
                element.render('nav', 'admin-side-nav');
            }
        });
        $(sideDOM + '>.layui-nav').html(html);
        element.render('nav', 'admin-side-nav');
    };

    /** 移动设备遮罩层 */
    var siteShadeDom = '.layui-layout-admin .site-mobile-shade';
    if ($(siteShadeDom).length === 0) $('.layui-layout-admin').append('<div class="site-mobile-shade"></div>');
    $(siteShadeDom).click(function () {
        admin.flexible(true);
    });

    /** 补充tab的dom */
    if (setter.pageTabs && $(tabDOM).length === 0) {
        $(bodyDOM).html([
            '<div class="layui-tab" lay-allowClose="true" lay-filter="', tabFilter, '" lay-autoRefresh="', setter.tabAutoRefresh == true, '">',
            '   <ul class="layui-tab-title"></ul><div class="layui-tab-content"></div>',
            '</div>',
            '<div class="layui-icon admin-tabs-control layui-icon-prev" ew-event="leftPage"></div>',
            '<div class="layui-icon admin-tabs-control layui-icon-next" ew-event="rightPage"></div>',
            '<div class="layui-icon admin-tabs-control layui-icon-down">',
            '   <ul class="layui-nav" lay-filter="admin-pagetabs-nav">',
            '      <li class="layui-nav-item" lay-unselect>',
            '         <dl class="layui-nav-child layui-anim-fadein">',
            '            <dd ew-event="closeThisTabs" lay-unselect><a>关闭当前标签页</a></dd>',
            '            <dd ew-event="closeOtherTabs" lay-unselect><a>关闭其它标签页</a></dd>',
            '            <dd ew-event="closeAllTabs" lay-unselect><a>关闭全部标签页</a></dd>',
            '         </dl>',
            '      </li>',
            '   </ul>',
            '</div>'
        ].join(''));
        element.render('nav', 'admin-pagetabs-nav');
    }

    /** 侧导航点击监听 */
    element.on('nav(' + navFilter + ')', function (elem) {
        var href = $(elem).attr('href');
        if (href.indexOf('#') !== 0 || $(tabDOM).attr('lay-autoRefresh') != 'true') return;
        if (!mIsAddTab) admin.refresh(href.substring(1), true);  // 切换tab刷新
    });

    /** tab切换监听 */
    element.on('tab(' + tabFilter + ')', function () {
        var hash = $(this).attr('lay-id');
        var url = $(tabDOM + '>.layui-tab-content>.layui-tab-item>div[lay-id="' + hash + '"]').attr('lay-url');
        admin.activeNav(url);
        admin.rollPage('auto');
        if ($(tabDOM).attr('lay-autoRefresh') == 'true' && !mIsAddTab) admin.refresh(url, true);  // 切换tab刷新
        else layRouter.go(url);  // 改变hash地址
        mIsAddTab = false;
        admin.resizeTable(0);
        layui.event.call(this, 'admin', 'show(' + hash + ')');
        layui.event.call(this, 'admin', 'tab({*})', {layId: hash});
    });

    /** tab删除监听 */
    element.on('tabDelete(' + tabFilter + ')', function (data) {
        if (data.index > 0 && data.index <= index.mTabList.length) {
            var hashPath = index.getHashPath(index.mTabList[data.index - 1].href);
            layui.event.call(this, 'admin', 'destroy(' + hashPath + ')');  // 页面卸载回调
            index.mTabList.splice(data.index - 1, 1);
            if (setter.cacheTab) admin.putTempData('indexTabs', index.mTabList);
            layui.event.call(this, 'admin', 'tabDelete({*})', {layId: hashPath});
        }
        if ($(tabDOM + '>.layui-tab-title>li.layui-this').length === 0)
            $(tabDOM + '>.layui-tab-title>li:last').trigger('click');  // 解决删除后可能无选中bug
    });

    /** 多系统切换事件 */
    $(document).off('click.navMore').on('click.navMore', '[nav-bind]', function () {
        var navId = $(this).attr('nav-bind');
        $('ul[lay-filter="' + navFilter + '"]').addClass('layui-hide');
        $('ul[nav-id="' + navId + '"]').removeClass('layui-hide');
        $(headerDOM + '>.layui-nav .layui-nav-item').removeClass('layui-this');
        $(this).parent('.layui-nav-item').addClass('layui-this');
        if (admin.getPageWidth() <= 768) admin.flexible(false);  // 展开侧边栏
        layui.event.call(this, 'admin', 'nav({*})', {navId: navId});
    });

    /** 开启Tab右键菜单 */
    if (setter.openTabCtxMenu && setter.pageTabs) {
        layui.use('contextMenu', function () {
            if (!layui.contextMenu) return;
            $(tabDOM + '>.layui-tab-title').off('contextmenu.tab').on('contextmenu.tab', 'li', function (e) {
                var layId = $(this).attr('lay-id');
                layui.contextMenu.show([{
                    icon: 'layui-icon layui-icon-refresh',
                    name: '刷新当前',
                    click: function () {
                        element.tabChange(tabFilter, layId);
                        var url = $(tabDOM + '>.layui-tab-content>.layui-tab-item>div[lay-id="' + layId + '"]').attr('lay-url');
                        if ('true' != $(tabDOM).attr('lay-autoRefresh')) admin.refresh(url);
                    }
                }, {
                    icon: 'layui-icon layui-icon-close-fill ctx-ic-lg',
                    name: '关闭当前',
                    click: function () {
                        admin.closeThisTabs(layId);
                    }
                }, {
                    icon: 'layui-icon layui-icon-unlink',
                    name: '关闭其他',
                    click: function () {
                        admin.closeOtherTabs(layId);
                    }
                }, {
                    icon: 'layui-icon layui-icon-close ctx-ic-lg',
                    name: '关闭全部',
                    click: function () {
                        admin.closeAllTabs();
                    }
                }], e.clientX, e.clientY);
                return false;
            });
        });
    }

    exports('index', index);
});
