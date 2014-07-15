'use strict';

/**
 * Returns or set a nested property by path.
 * @param obj Object
 * @param propString Property path
 * @param value Value to set
 * @returns {*} Nested property
 */
var propByPath = function(obj, propString, value) {
  if (!propString)
    return obj;

  var prop, props = propString.split('.');

  for (var i = 0, iLen = props.length - 1; i < iLen; i++) {
    prop = props[i];

    var candidate = obj[prop];
    if (candidate !== undefined) {
      obj = candidate;
    } else {
      break;
    }
  }

  if (value !== undefined) {
    obj[props[i]] = value;
  } else {
    return obj[props[i]];
  }
}