!function ($) {
    "use strict";
    var ModalManager = function (element, options) {
        this.init(element, options)
    };
    ModalManager.prototype = {
        constructor: ModalManager, init: function (element, options) {
            this.$element = $(element);
            this.options = $.extend({}, $.fn.modalmanager.defaults, this.$element.data(), typeof options == "object" && options);
            this.stack = [];
            this.backdropCount = 0;
            if (this.options.resize) {
                var resizeTimeout, that = this;
                $(window).on("resize.modal", function () {
                    resizeTimeout && clearTimeout(resizeTimeout);
                    resizeTimeout = setTimeout(function () {
                        for (var i = 0; i < that.stack.length; i++) {
                            that.stack[i].isShown && that.stack[i].layout()
                        }
                    }, 10)
                })
            }
        }, createModal: function (element, options) {
            $(element).modal($.extend({manager: this}, options))
        }, appendModal: function (modal) {
            this.stack.push(modal);
            var that = this;
            modal.$element.on("show.modalmanager", targetIsSelf(function (e) {
                var showModal = function () {
                    modal.isShown = true;
                    var transition = $.support.transition && modal.$element.hasClass("fade");
                    that.$element.toggleClass("modal-open", that.hasOpenModal()).toggleClass("page-overflow", $(window).height() < that.$element.height());
                    modal.$parent = modal.$element.parent();
                    modal.$container = that.createContainer(modal);
                    modal.$element.appendTo(modal.$container);
                    that.backdrop(modal, function () {
                        modal.$element.show();
                        if (transition) {
                            modal.$element[0].offsetWidth
                        }
                        modal.layout();
                        modal.$element.addClass("in").attr("aria-hidden", false);
                        var complete = function () {
                            that.setFocus();
                            modal.$element.trigger("shown")
                        };
                        transition ? modal.$element.one($.support.transition.end, complete) : complete()
                    })
                };
                modal.options.replace ? that.replace(showModal) : showModal()
            }));
            modal.$element.on("hidden.modalmanager", targetIsSelf(function (e) {
                that.backdrop(modal);
                if (modal.$backdrop) {
                    $.support.transition && modal.$element.hasClass("fade") ? modal.$backdrop.one($.support.transition.end, function () {
                        that.destroyModal(modal)
                    }) : that.destroyModal(modal)
                } else {
                    that.destroyModal(modal)
                }
            }));
            modal.$element.on("destroy.modalmanager", targetIsSelf(function (e) {
                that.removeModal(modal)
            }))
        }, destroyModal: function (modal) {
            modal.destroy();
            var hasOpenModal = this.hasOpenModal();
            this.$element.toggleClass("modal-open", hasOpenModal);
            if (!hasOpenModal) {
                this.$element.removeClass("page-overflow")
            }
            this.removeContainer(modal);
            this.setFocus()
        }, hasOpenModal: function () {
            for (var i = 0; i < this.stack.length; i++) {
                if (this.stack[i].isShown)return true
            }
            return false
        }, setFocus: function () {
            var topModal;
            for (var i = 0; i < this.stack.length; i++) {
                if (this.stack[i].isShown)topModal = this.stack[i]
            }
            if (!topModal)return;
            topModal.focus()
        }, removeModal: function (modal) {
            modal.$element.off(".modalmanager");
            if (modal.$backdrop)this.removeBackdrop.call(modal);
            this.stack.splice(this.getIndexOfModal(modal), 1)
        }, getModalAt: function (index) {
            return this.stack[index]
        }, getIndexOfModal: function (modal) {
            for (var i = 0; i < this.stack.length; i++) {
                if (modal === this.stack[i])return i
            }
        }, replace: function (callback) {
            var topModal;
            for (var i = 0; i < this.stack.length; i++) {
                if (this.stack[i].isShown)topModal = this.stack[i]
            }
            if (topModal) {
                this.$backdropHandle = topModal.$backdrop;
                topModal.$backdrop = null;
                callback && topModal.$element.one("hidden", targetIsSelf($.proxy(callback, this)));
                topModal.hide()
            } else if (callback) {
                callback()
            }
        }, removeBackdrop: function (modal) {
            modal.$backdrop.remove();
            modal.$backdrop = null
        }, createBackdrop: function (animate) {
            var $backdrop;
            if (!this.$backdropHandle) {
                $backdrop = $('<div class="modal-backdrop ' + animate + '" />').appendTo(this.$element)
            } else {
                $backdrop = this.$backdropHandle;
                $backdrop.off(".modalmanager");
                this.$backdropHandle = null;
                this.isLoading && this.removeSpinner()
            }
            return $backdrop
        }, removeContainer: function (modal) {
            modal.$container.remove();
            modal.$container = null
        }, createContainer: function (modal) {
            var $container;
            $container = $('<div class="modal-scrollable">').css("z-index", getzIndex("modal", modal ? this.getIndexOfModal(modal) : this.stack.length)).appendTo(this.$element);
            if (modal && modal.options.backdrop != "static") {
                $container.on("click.modal", targetIsSelf(function (e) {
                    modal.hide()
                }))
            } else if (modal) {
                $container.on("click.modal", targetIsSelf(function (e) {
                    modal.attention()
                }))
            }
            return $container
        }, backdrop: function (modal, callback) {
            var animate = modal.$element.hasClass("fade") ? "fade" : "", showBackdrop = modal.options.backdrop && this.backdropCount < this.options.backdropLimit;
            if (modal.isShown && showBackdrop) {
                var doAnimate = $.support.transition && animate && !this.$backdropHandle;
                modal.$backdrop = this.createBackdrop(animate);
                modal.$backdrop.css("z-index", getzIndex("backdrop", this.getIndexOfModal(modal)));
                if (doAnimate)modal.$backdrop[0].offsetWidth;
                modal.$backdrop.addClass("in");
                this.backdropCount += 1;
                doAnimate ? modal.$backdrop.one($.support.transition.end, callback) : callback()
            } else if (!modal.isShown && modal.$backdrop) {
                modal.$backdrop.removeClass("in");
                this.backdropCount -= 1;
                var that = this;
                $.support.transition && modal.$element.hasClass("fade") ? modal.$backdrop.one($.support.transition.end, function () {
                    that.removeBackdrop(modal)
                }) : that.removeBackdrop(modal)
            } else if (callback) {
                callback()
            }
        }, removeSpinner: function () {
            this.$spinner && this.$spinner.remove();
            this.$spinner = null;
            this.isLoading = false
        }, removeLoading: function () {
            this.$backdropHandle && this.$backdropHandle.remove();
            this.$backdropHandle = null;
            this.removeSpinner()
        }, loading: function (callback) {
            callback = callback || function () {
                };
            this.$element.toggleClass("modal-open", !this.isLoading || this.hasOpenModal()).toggleClass("page-overflow", $(window).height() < this.$element.height());
            if (!this.isLoading) {
                this.$backdropHandle = this.createBackdrop("fade");
                this.$backdropHandle[0].offsetWidth;
                this.$backdropHandle.css("z-index", getzIndex("backdrop", this.stack.length)).addClass("in");
                var $spinner = $(this.options.spinner).css("z-index", getzIndex("modal", this.stack.length)).appendTo(this.$element).addClass("in");
                this.$spinner = $(this.createContainer()).append($spinner).on("click.modalmanager", $.proxy(this.loading, this));
                this.isLoading = true;
                $.support.transition ? this.$backdropHandle.one($.support.transition.end, callback) : callback()
            } else if (this.isLoading && this.$backdropHandle) {
                this.$backdropHandle.removeClass("in");
                var that = this;
                $.support.transition ? this.$backdropHandle.one($.support.transition.end, function () {
                    that.removeLoading()
                }) : that.removeLoading()
            } else if (callback) {
                callback(this.isLoading)
            }
        }
    };
    var getzIndex = function () {
        var zIndexFactor, baseIndex = {};
        return function (type, pos) {
            if (typeof zIndexFactor === "undefined") {
                var $baseModal = $('<div class="modal hide" />').appendTo("body"), $baseBackdrop = $('<div class="modal-backdrop hide" />').appendTo("body");
                baseIndex["modal"] = +$baseModal.css("z-index");
                baseIndex["backdrop"] = +$baseBackdrop.css("z-index");
                zIndexFactor = baseIndex["modal"] - baseIndex["backdrop"];
                $baseModal.remove();
                $baseBackdrop.remove();
                $baseBackdrop = $baseModal = null
            }
            return baseIndex[type] + zIndexFactor * pos
        }
    }();

    function targetIsSelf(callback) {
        return function (e) {
            if (this === e.target) {
                return callback.apply(this, arguments)
            }
        }
    }

    $.fn.modalmanager = function (option, args) {
        return this.each(function () {
            var $this = $(this), data = $this.data("modalmanager");
            if (!data)$this.data("modalmanager", data = new ModalManager(this, option));
            if (typeof option === "string")data[option].apply(data, [].concat(args))
        })
    };
    $.fn.modalmanager.defaults = {
        backdropLimit: 999,
        resize: true,
        spinner: '<div class="loading-spinner fade" style="width: 200px; margin-left: -100px;"><div class="progress progress-striped active"><div class="bar" style="width: 100%;"></div></div></div>'
    };
    $.fn.modalmanager.Constructor = ModalManager
}(jQuery);
!function ($) {
    "use strict";
    var Modal = function (element, options) {
        this.init(element, options)
    };
    Modal.prototype = {
        constructor: Modal, init: function (element, options) {
            this.options = options;
            this.$element = $(element).delegate('[data-dismiss="modal"]', "click.dismiss.modal", $.proxy(this.hide, this));
            this.options.remote && this.$element.find(".modal-body").load(this.options.remote);
            var manager = typeof this.options.manager === "function" ? this.options.manager.call(this) : this.options.manager;
            manager = manager.appendModal ? manager : $(manager).modalmanager().data("modalmanager");
            manager.appendModal(this)
        }, toggle: function () {
            return this[!this.isShown ? "show" : "hide"]()
        }, show: function () {
            var e = $.Event("show");
            if (this.isShown)return;
            this.$element.trigger(e);
            if (e.isDefaultPrevented())return;
            this.escape();
            this.tab();
            this.options.loading && this.loading()
        }, hide: function (e) {
            e && e.preventDefault();
            e = $.Event("hide");
            this.$element.trigger(e);
            if (!this.isShown || e.isDefaultPrevented())return this.isShown = false;
            this.isShown = false;
            this.escape();
            this.tab();
            this.isLoading && this.loading();
            $(document).off("focusin.modal");
            this.$element.removeClass("in").removeClass("animated").removeClass(this.options.attentionAnimation).removeClass("modal-overflow").attr("aria-hidden", true);
            $.support.transition && this.$element.hasClass("fade") ? this.hideWithTransition() : this.hideModal()
        }, layout: function () {
            var prop = this.options.height ? "height" : "max-height", value = this.options.height || this.options.maxHeight;
            if (this.options.width) {
                this.$element.css("width", this.options.width);
                var that = this;
                this.$element.css("margin-left", function () {
                    if (/%/gi.test(that.options.width)) {
                        return -(parseInt(that.options.width) / 2) + "%"
                    } else {
                        return -($(this).width() / 2) + "px"
                    }
                })
            } else {
                this.$element.css("width", "");
                this.$element.css("margin-left", "")
            }
            this.$element.find(".modal-body").css("overflow", "").css(prop, "");
            var modalOverflow = $(window).height() - 10 < this.$element.height();
            if (value) {
                this.$element.find(".modal-body").css("overflow", "auto").css(prop, value)
            }
            if (modalOverflow || this.options.modalOverflow) {
                this.$element.css("margin-top", 0).addClass("modal-overflow")
            } else {
                this.$element.css("margin-top", 0 - this.$element.height() / 2).removeClass("modal-overflow")
            }
        }, tab: function () {
            var that = this;
            if (this.isShown && this.options.consumeTab) {
                this.$element.on("keydown.tabindex.modal", "[data-tabindex]", function (e) {
                    if (e.keyCode && e.keyCode == 9) {
                        var $next = $(this), $rollover = $(this);
                        that.$element.find("[data-tabindex]:enabled:not([readonly])").each(function (e) {
                            if (!e.shiftKey) {
                                $next = $next.data("tabindex") < $(this).data("tabindex") ? $next = $(this) : $rollover = $(this)
                            } else {
                                $next = $next.data("tabindex") > $(this).data("tabindex") ? $next = $(this) : $rollover = $(this)
                            }
                        });
                        $next[0] !== $(this)[0] ? $next.focus() : $rollover.focus();
                        e.preventDefault()
                    }
                })
            } else if (!this.isShown) {
                this.$element.off("keydown.tabindex.modal")
            }
        }, escape: function () {
            var that = this;
            if (this.isShown && this.options.keyboard) {
                if (!this.$element.attr("tabindex"))this.$element.attr("tabindex", -1);
                this.$element.on("keyup.dismiss.modal", function (e) {
                    e.which == 27 && that.hide()
                })
            } else if (!this.isShown) {
                this.$element.off("keyup.dismiss.modal")
            }
        }, hideWithTransition: function () {
            var that = this, timeout = setTimeout(function () {
                that.$element.off($.support.transition.end);
                that.hideModal()
            }, 500);
            this.$element.one($.support.transition.end, function () {
                clearTimeout(timeout);
                that.hideModal()
            })
        }, hideModal: function () {
            this.$element.hide().trigger("hidden");
            var prop = this.options.height ? "height" : "max-height";
            var value = this.options.height || this.options.maxHeight;
            if (value) {
                this.$element.find(".modal-body").css("overflow", "").css(prop, "")
            }
        }, removeLoading: function () {
            this.$loading.remove();
            this.$loading = null;
            this.isLoading = false
        }, loading: function (callback) {
            callback = callback || function () {
                };
            var animate = this.$element.hasClass("fade") ? "fade" : "";
            if (!this.isLoading) {
                var doAnimate = $.support.transition && animate;
                this.$loading = $('<div class="loading-mask ' + animate + '">').append(this.options.spinner).appendTo(this.$element);
                if (doAnimate)this.$loading[0].offsetWidth;
                this.$loading.addClass("in");
                this.isLoading = true;
                doAnimate ? this.$loading.one($.support.transition.end, callback) : callback()
            } else if (this.isLoading && this.$loading) {
                this.$loading.removeClass("in");
                var that = this;
                $.support.transition && this.$element.hasClass("fade") ? this.$loading.one($.support.transition.end, function () {
                    that.removeLoading()
                }) : that.removeLoading()
            } else if (callback) {
                callback(this.isLoading)
            }
        }, focus: function () {
            var $focusElem = this.$element.find(this.options.focusOn);
            $focusElem = $focusElem.length ? $focusElem : this.$element;
            $focusElem.focus()
        }, attention: function () {
            if (this.options.attentionAnimation) {
                this.$element.removeClass("animated").removeClass(this.options.attentionAnimation);
                var that = this;
                setTimeout(function () {
                    that.$element.addClass("animated").addClass(that.options.attentionAnimation)
                }, 0)
            }
            this.focus()
        }, destroy: function () {
            var e = $.Event("destroy");
            this.$element.trigger(e);
            if (e.isDefaultPrevented())return;
            this.teardown()
        }, teardown: function () {
            if (!this.$parent.length) {
                this.$element.remove();
                this.$element = null;
                return
            }
            if (this.$parent !== this.$element.parent()) {
                this.$element.appendTo(this.$parent)
            }
            this.$element.off(".modal");
            this.$element.removeData("modal");
            this.$element.removeClass("in").attr("aria-hidden", true)
        }
    };
    $.fn.modal = function (option, args) {
        return this.each(function () {
            var $this = $(this), data = $this.data("modal"), options = $.extend({}, $.fn.modal.defaults, $this.data(), typeof option == "object" && option);
            if (!data)$this.data("modal", data = new Modal(this, options));
            if (typeof option == "string")data[option].apply(data, [].concat(args)); else if (options.show)data.show()
        })
    };
    $.fn.modal.defaults = {
        keyboard: true,
        backdrop: true,
        loading: false,
        show: true,
        width: null,
        height: null,
        maxHeight: null,
        modalOverflow: false,
        consumeTab: true,
        focusOn: null,
        replace: false,
        resize: false,
        attentionAnimation: "shake",
        manager: "body",
        spinner: '<div class="loading-spinner" style="width: 200px; margin-left: -100px;"><div class="progress progress-striped active"><div class="bar" style="width: 100%;"></div></div></div>'
    };
    $.fn.modal.Constructor = Modal;
    $(function () {
        $(document).off("click.modal").on("click.modal.data-api", '[data-toggle="modal"]', function (e) {
            var $this = $(this), href = $this.attr("href"), $target = $($this.attr("data-target") || href && href.replace(/.*(?=#[^\s]+$)/, "")), option = $target.data("modal") ? "toggle" : $.extend({remote: !/#/.test(href) && href}, $target.data(), $this.data());
            e.preventDefault();
            $target.modal(option).one("hide", function () {
                $this.focus()
            })
        })
    })
}(window.jQuery);