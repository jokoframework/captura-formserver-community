// consider replacing with http://ned.im/noty/
define(["jquery", "jquery-ui"], function($, $ui) {
	/* jQuery Notify UI Widget 1.5 by Eric Hynds
	 * http://www.erichynds.com/jquery/a-jquery-ui-growl-ubuntu-notification-widget/
	 *
	 * Depends:
	 *   - jQuery 1.4+
	 *   - jQuery UI 1.8 widget factory
	 *
	 * Dual licensed under the MIT and GPL licenses:
	 *   http://www.opensource.org/licenses/mit-license.php
	 *   http://www.gnu.org/licenses/gpl.html
	*/
	(function($){
	
	  $.widget("ech.notify", {
	
	    options: {
	      speed: 500,
	      expires: 5000,
	      stack: "below",
	      custom: false,
	      queue: false
	    },
	
	    _create: function(){
	      var self = this;
	      this.templates = {};
	      this.keys = [];
	
	      // build and save templates
	      this.element.addClass("ui-notify").children().addClass("ui-notify-message ui-notify-message-style").each(function(i){
	        var key = this.id || i;
	        self.keys.push(key);
	        self.templates[key] = $(this).removeAttr("id").wrap("<div></div>").parent().html(); // because $(this).andSelf().html() no workie
	      }).end().empty().show();
	    },
	
	    create: function(template, msg, opts){
	      if(typeof template === "object"){
	        opts = msg;
	        msg = template;
	        template = null;
	      }
	
	      var tpl = this.templates[ template || this.keys[0]];
	
	      // remove default styling class if rolling w/ custom classes
	      if(opts && opts.custom){
	        tpl = $(tpl).removeClass("ui-notify-message-style").wrap("<div></div>").parent().html();
	      }
	
	      this.openNotifications = this.openNotifications || 0;
	
	      // return a new notification instance
	      return new $.ech.notify.instance(this)._create(msg, $.extend({}, this.options, opts), tpl);            
	    }
	  });
	
	  // instance constructor
	  $.extend($.ech.notify, {
	    instance: function(widget){
	      this.__super = widget;
	      this.isOpen = false;
	    }
	  });
	
	  // instance methods
	  $.extend($.ech.notify.instance.prototype, {
	
	    _create: function(params, options, template){
	      this.options = options;
	
	      var self = this,
	
	      // build html template
	      html = template.replace(/t(?:\{|%7B)(.*?)(?:\}|%7D)/g, function($1, $2){
	        return ($2 in params) ? params[$2] : '';
	      }),
	
	      // the actual message
	      m = (this.element = $(html)),
	
	      // close link
	      closelink = m.find(".ui-notify-close");
	
	      // clickable?
	      if(typeof this.options.click === "function"){
	        m.addClass("ui-notify-click").bind("click", function(e){
	          self._trigger("click", e, self);
	        });
	      }
	
	      // show close link?
	      if(closelink.length){
	        closelink.bind("click", function(){
	          self.close();
	          return false;
	        });
	      }
	
	      this.__super.element.queue("notify", function(){
	        self.open();
	
	        // auto expire?
	        if(typeof options.expires === "number" && options.expires > 0){
	          setTimeout($.proxy(self.close, self), options.expires);
	        }
	      });
	
	      if(!this.options.queue || this.__super.openNotifications <= this.options.queue - 1) {
	        this.__super.element.dequeue("notify");
	      }
	
	      return this;
	    },
	
	    close: function(){
	      var speed = this.options.speed;
	
	      this.element.fadeTo(speed, 0).slideUp(speed, $.proxy(function(){
	        this._trigger("close");
	        this.isOpen = false;
	        this.element.remove();
	        this.__super.openNotifications -= 1;
	        this.__super.element.dequeue("notify");
	      }, this));
	
	      return this;
	    },
	
	    open: function(){
	      if(this.isOpen || this._trigger("beforeopen") === false){
	        return this;
	      }
	
	      var self = this;
	
	      this.__super.openNotifications += 1;
	
	      this.element[this.options.stack === "above" ? "prependTo" : "appendTo"](this.__super.element).css({ display:"none", opacity:"" }).fadeIn(this.options.speed, function(){
	        self._trigger("open");
	        self.isOpen = true;
	      });
	
	      return this;
	    },
	
	    widget: function(){
	      return this.element;
	    },
	
	    _trigger: function(type, e, instance){
	      return this.__super._trigger.call( this, type, e, instance );
	    }
	  });
	
	})(jQuery);
});
