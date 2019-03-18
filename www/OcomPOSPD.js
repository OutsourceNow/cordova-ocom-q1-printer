exec = require('cordova/exec');

// var IPosPrinterTestDemo = function() {
//   console.log('AndroidToast instanced');
// };

// IPosPrinterTestDemo.prototype.show = function(msg, onSuccess, onError) {
//   var errorCallback = function(obj) {
//       onError(obj);
//   };

//   var successCallback = function(obj) {
//       onSuccess(obj);
//   };

//   exec(successCallback, errorCallback, 'IPosPrinterTestDemo', 'show', [msg]);
// };

// if (typeof module != 'undefined' && module.exports) {
//   module.exports = AndroidToast;
// }

module.exports = {
  show: function (msg, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "show", [msg]);
  },
  printerInit: function (resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "printerInit", []);
  },
  printerSelfChecking: function (resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "printerSelfChecking", []);
  },
  getPrinterSerialNo: function (resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "getPrinterSerialNo", []);
  },
  getPrinterVersion: function (resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "getPrinterVersion", []);
  },
  hasPrinter: function (resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "hasPrinter", []);
  },
  getPrintedLength: function (resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "getPrintedLength", []);
  },
  lineWrap: function (count, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "lineWrap", [count]);
  },
  sendRAWData: function (base64Data, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "sendRAWData", [base64Data]);
  },
  setAlignment: function (alignment, resolve, reject) {
    exec(resolve, reject, "Printer", "setAlignment", [alignment]);
  },
  setFontName: function (typeface, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "setFontName", [typeface]);
  },
  setFontSize: function (fontSize, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "setFontSize", [fontSize]);
  },
  printTextWithFont: function (text, typeface, fontSize, resolve, reject) {
    exec(resolve, reject, "Printer", "printTextWithFont", [text, typeface, fontSize]);
  },
  printColumnsText: function (colsTextArr, colsWidthArr, colsAlign, resolve, reject) {
    exec(resolve, reject, "Printer", "printColumnsText", [colsTextArr, colsWidthArr, colsAlign]);
  },
  printBitmap: function (base64Data, width, height, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "printBitmap", [base64Data, width, height]);
  },
  printBarCode: function (barCodeData, symbology, width, height, textPosition, resolve, reject) {
    exec(resolve, reject, "Printer", "printBarCode", [barCodeData, symbology, width, height, textPosition]);
  },
  printQRCode: function (qrCodeData, moduleSize, errorLevel, resolve, reject) {
    exec(resolve, reject, "Printer", "printQRCode", [qrCodeData, moduleSize, errorLevel]);
  },
  printOriginalText: function (text, resolve, reject) {
    exec(resolve, reject, "Printer", "printOriginalText", [text]);
  },
  printString: function (text, resolve, reject) {
    exec(resolve, reject, "IPosPrinterTestDemo", "printString", [text]);
  },
  printerStatusStartListener: function (onSuccess, onError) {
    exec(onSuccess, onError, "IPosPrinterTestDemo", "printerStatusStartListener", []);
  },
  printerStatusStopListener: function () {
    exec(function () {}, function () {}, "IPosPrinterTestDemo", "printerStatusStopListener", []);
  }

}
