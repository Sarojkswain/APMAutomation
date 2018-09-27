(function($) {
  /*
   * Simulate drag and drop
   */
  $.fn.simulateDragSortable = function(dragHandleLocator, dropElementLocator) {

	var dragElement = $(this).find(dragHandleLocator)[0];
    var dropElement = $(dropElementLocator)[0];

    if (!dragElement || !dropElement) {
      return;
    }

    var step = 10;
    var moveInterval = 10;
    var mouseUpInterval = 1000;

    var dragElementPosition = getPosition(dragElement);
    var dropElementPosition = getPosition(dropElement);

    var x = dragElementPosition.cx;
    var y = dragElementPosition.cy;

    /* We define a series of target drop points in order to make the tile really get up and do the move. */
    var targets = [];
    /* Center of the tile */
    targets[0] = {x: dropElementPosition.cx, y: dropElementPosition.cy};
    /* Top left corner of the tile */
    targets[1] = {x: dropElementPosition.l, y: dropElementPosition.t};
    /* Top right corner of the tile */
    targets[2] = {x: dropElementPosition.l + dropElementPosition.w, y: dropElementPosition.t};
    /* Bottom right corner of the tile */
    targets[3] = {x: dropElementPosition.l + dropElementPosition.w, y: dropElementPosition.t + dropElementPosition.h};
    /* Bottom left corner of the tile */
    targets[4] = {x: dropElementPosition.l, y: dropElementPosition.t + dropElementPosition.h};
    /* Center of the tile */
    targets[5] = {x: dropElementPosition.cx, y: dropElementPosition.cy};
    
    /* Simulate click on the drag element */
    dispatchEvent(dragElement, 'mousedown', createEvent('mousedown', dragElement, {clientX: x, clientY: y}));

    var opts = {
      dragElement: dragElement,
      targets: targets,
      step: step,
      moveInterval: moveInterval,
      mouseUpInterval: mouseUpInterval
    };

    /* Move until we are on the target */
    moveToRecursively(x, y, 0, opts);
  };

  function moveToRecursively(x, y, currTgtNdx, opts) {
    var tx = opts.targets[currTgtNdx].x;
    var ty = opts.targets[currTgtNdx].y;

    if (Math.abs(x - tx) < opts.step) {
      x = tx;
    }

    if (Math.abs(y - ty) < opts.step) {
      y = ty;
    }

    if ((x == tx) && (y == ty)) {
      /* If the last defined target was reached */
      if (currTgtNdx == (opts.targets.length - 1)) {
        /* Simulate dropping */
        setTimeout(function() {
          dispatchEvent(opts.dragElement, 'mouseup', createEvent('mouseup', opts.dragElement, {clientX: x, clientY: y}));
        }, opts.mouseUpInterval);
      } else {
        /* Aim to the next target */
        moveToRecursively(x, y, currTgtNdx + 1, opts);
      }
    } else {
      x += x < tx ? opts.step : (x > tx ? -opts.step : 0);
      y += y < ty ? opts.step : (y > ty ? -opts.step : 0);

      setTimeout(function() {
        dispatchEvent(document, 'mousemove', createEvent('mousemove', document, {clientX: x, clientY: y}));

        moveToRecursively(x, y, currTgtNdx, opts);
      }, opts.interval);
    }
  }

  function createEvent(type, target, options) {
    var evt;
    var e = $.extend({
      target: target,
      preventDefault: function() {
      },
      stopImmediatePropagation: function() {
      },
      stopPropagation: function() {
      },
      isPropagationStopped: function() {
        return true;
      },
      isImmediatePropagationStopped: function() {
        return true;
      },
      isDefaultPrevented: function() {
        return true;
      },
      bubbles: true,
      cancelable: (type != "mousemove"),
      view: window,
      detail: 0,
      screenX: 0,
      screenY: 0,
      clientX: 0,
      clientY: 0,
      ctrlKey: false,
      altKey: false,
      shiftKey: false,
      metaKey: false,
      button: 0,
      relatedTarget: undefined
    }, options || {});

    if ($.isFunction(document.createEvent)) {
      evt = document.createEvent("MouseEvents");
      evt.initMouseEvent(type, e.bubbles, e.cancelable, e.view, e.detail,
          e.screenX, e.screenY, e.clientX, e.clientY,
          e.ctrlKey, e.altKey, e.shiftKey, e.metaKey,
          e.button, e.relatedTarget || document.body.parentNode);
    } else if (document.createEventObject) {
      evt = document.createEventObject();
      $.extend(evt, e);
      evt.button = {0: 1, 1: 4, 2: 2}[evt.button] || evt.button;
    }
    return evt;
  }

  function dispatchEvent(el, type, evt) {
    if (el.dispatchEvent) {
      el.dispatchEvent(evt);
    } else if (el.fireEvent) {
      el.fireEvent('on' + type, evt);
    }
    return evt;
  }

  function getPosition(el) {
    var elm = $(el),
        o = elm.offset();
    return {
      l: o.left,
      t: o.top,
      w: elm.outerWidth(),
      h: elm.outerHeight(),
      cx: o.left + elm.outerWidth() / 2,
      cy: o.top + elm.outerHeight() / 2
    };
  }
})(jQuery);
