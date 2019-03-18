package com.iposprinter.printertestdemo;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import com.iposprinter.iposprinterservice.aidl.IPosPrinterCallback;
import com.iposprinter.iposprinterservice.aidl.IPosPrinterService;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.ServiceConnection;

import android.graphics.Bitmap;

import android.os.IBinder;

import android.util.Base64;
import android.util.Log;

import com.iposprinter.printertestdemo.Utils.BitmapUtils;
import com.iposprinter.printertestdemo.ThreadPoolManager;
import android.com.iposprinter.printertestdemo.PrinterStatusReceiver;


public class IPosPrinterTestDemo extends CordovaPlugin {

    private static final String TAG = "IPosPrinterTestDemo";
    /* Demo 版本号 */
    private static final String VERSION = "V1.1.1";

    private BitmapUtils bitMapUtils;
    private IPosPrinterService iPosPrinterService;
    private PrinterStatusReceiver printerStatusReceiver = new PrinterStatusReceiver();

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            iPosPrinterService = null;
            Log.d(TAG, "Service disconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iPosPrinterService = IPosPrinterService.Stub.asInterface(service);
            Log.d(TAG, "Service connected");
        }
    };

    /* 定义打印机状态 */
    private final int PRINTER_NORMAL = 0;
    private final int PRINTER_PAPERLESS = 1;
    private final int PRINTER_THP_HIGH_TEMPERATURE = 2;
    private final int PRINTER_MOTOR_HIGH_TEMPERATURE = 3;
    private final int PRINTER_IS_BUSY = 4;
    private final int PRINTER_ERROR_UNKNOWN = 5;
    /* 打印机当前状态 */
    private int printerStatus = 0;

    /* 定义状态广播 */
    private final String PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.aidl.NORMAL_ACTION";
    private final String PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.aidl.PAPERLESS_ACTION";
    private final String PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.aidl.PAPEREXISTS_ACTION";
    private final String PRINTER_THP_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.aidl.THP_HIGHTEMP_ACTION";
    private final String PRINTER_THP_NORMALTEMP_ACTION = "com.iposprinter.iposprinterservice.aidl.THP_NORMALTEMP_ACTION";
    private final String PRINTER_MOTOR_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.aidl.MOTOR_HIGHTEMP_ACTION";
    private final String PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.aidl.BUSY_ACTION";
    private final String PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION = "com.iposprinter.iposprinterservice.aidl.CURRENT_TASK_PRINT_COMPLETE_ACTION";
    private final String GET_CUST_PRINTAPP_PACKAGENAME_ACTION = "android.print.action.CUST_PRINTAPP_PACKAGENAME";

    /* 定义消息 */
    private final int MSG_TEST = 1;
    private final int MSG_IS_NORMAL = 2;
    private final int MSG_IS_BUSY = 3;
    private final int MSG_PAPER_LESS = 4;
    private final int MSG_PAPER_EXISTS = 5;
    private final int MSG_THP_HIGH_TEMP = 6;
    private final int MSG_THP_TEMP_NORMAL = 7;
    private final int MSG_MOTOR_HIGH_TEMP = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER = 9;
    private final int MSG_CURRENT_TASK_PRINT_COMPLETE = 10;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Context applicationContext = this.cordova.getActivity().getApplicationContext();

        bitMapUtils = new BitmapUtils(applicationContext);

        Intent intent = new Intent();
        intent.setPackage("android.com.iposprinter.iposprinterservice.aidl");
        intent.setAction("android.com.iposprinter.iposprinterservice.aidl.IPosPrinterService");

        applicationContext.startService(intent);
        applicationContext.bindService(intent, connService, Context.BIND_AUTO_CREATE);

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(PRINTER_NORMAL_ACTION);
        mFilter.addAction(PRINTER_BUSY_ACTION);
        mFilter.addAction(PRINTER_PAPERLESS_ACTION);
        mFilter.addAction(PRINTER_PAPEREXISTS_ACTION);
        mFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION);
        mFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION);
        mFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION);
        mFilter.addAction(PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION);
        mFilter.addAction(GET_CUST_PRINTAPP_PACKAGENAME_ACTION);

        applicationContext.registerReceiver(printerStatusReceiver, mFilter);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        if (action.equals("printerInit")) {
            printerInit(callbackContext);
            return true;
        
        } else if (action.equals("getPrinterStatus")) {
            getPrinterStatus(callbackContext);
            return true;
       
        } else if (action.equals("sendRAWData")) {
            sendRAWData(data.getString(0), callbackContext);
            return true;
        } else if (action.equals("setAlignment")) {
            setAlignment(data.getInt(0), callbackContext);
            return true;
        } else if (action.equals("setFontName")) {
            setFontName(data.getString(0), callbackContext);
            return true;
        } else if (action.equals("setFontSize")) {
            setFontSize((int) data.getDouble(0), callbackContext);
            return true;
        } else if (action.equals("printTextWithFont")) {
            printTextWithFont(data.getString(0), data.getString(1), (int) data.getDouble(2), callbackContext);
            return true;
        } else if (action.equals("printColumnsText")) {
            printColumnsText(data.getJSONArray(0), data.getJSONArray(1), data.getJSONArray(2), callbackContext);
            return true;
        // } else if (action.equals("printBitmap")) {
        //     printBitmap(data.getString(0), data.getInt(1), data.getInt(2), callbackContext);
        //     return true;
        } else if (action.equals("printBarCode")) {
            printBarCode(data.getString(0), data.getInt(1), data.getInt(2), data.getInt(1), data.getInt(2),
                    callbackContext);
            return true;
        } else if (action.equals("printQRCode")) {
            printQRCode(data.getString(0), data.getInt(1), data.getInt(2), callbackContext);
            return true;
        } else if (action.equals("printOriginalText")) {
            printOriginalText(data.getString(0), callbackContext);
            return true;
        } else if (action.equals("printString")) {
            printString(data.getString(0), callbackContext);
            return true;
        } else if (action.equals("printerStatusStartListener")) {
            printerStatusStartListener(callbackContext);
            return true;
        } else if (action.equals("printerStatusStopListener")) {
            printerStatusStopListener();
            return true;
        }

        return false;
    }

    public void printerInit(final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printerInit(new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    // public void printerSelfChecking(final CallbackContext callbackContext) {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.printerSelfChecking(new IPosPrinterCallback.Stub() {
    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             callbackContext.success("");
    //                         } else {
    //                             callbackContext.error(isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         callbackContext.success(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         callbackContext.error(msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 callbackContext.error(e.getMessage());
    //             }
    //         }
    //     });
    // }

    // public void getPrinterSerialNo(final CallbackContext callbackContext) {
    //     try {
    //         callbackContext.success(getPrinterSerialNo());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         callbackContext.error(e.getMessage());
    //     }
    // }

    // private String getPrinterSerialNo() throws Exception {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     return printerService.getPrinterSerialNo();
    // }

    public void getPrinterStatus(final CallbackContext callbackContext) {
        try {
            callbackContext.success(getPrinterStatus());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            callbackContext.error(e.getMessage());
        }
    }

    private int getPrinterStatus() throws Exception {
        final IPosPrinterService printerService = iPosPrinterService;
        return printerService.getPrinterStatus();
    }

    // public void getPrinterModal(final CallbackContext callbackContext) {
    //     try {
    //         callbackContext.success(getPrinterModal());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         callbackContext.error(e.getMessage());
    //     }
    // }

    // private String getPrinterModal() throws Exception {
    //     // Caution: This method is not fully test -- Januslo 2018-08-11
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     return printerService.getPrinterModal();
    // }

    // public void hasPrinter(final CallbackContext callbackContext) {
    //     try {
    //         callbackContext.success(hasPrinter());
    //     } catch (Exception e) {
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //         callbackContext.error(e.getMessage());
    //     }
    // }

    // private int hasPrinter() {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     final boolean hasPrinterService = printerService != null;
    //     return hasPrinterService ? 1 : 0;
    // }

    // public void getPrintedLength(final CallbackContext callbackContext) {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.getPrintedLength(new IPosPrinterCallback.Stub() {
    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             callbackContext.success("");
    //                         } else {
    //                             callbackContext.error(isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         callbackContext.success(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         callbackContext.error(msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 callbackContext.error(e.getMessage());
    //             }
    //         }
    //     });
    // }

    // public void lineWrap(int n, final CallbackContext callbackContext) {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     final int count = n;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.lineWrap(count, new IPosPrinterCallback.Stub() {
    //                     @Override
    //                     public void onRunResult(boolean isSuccess) {
    //                         if (isSuccess) {
    //                             callbackContext.success("");
    //                         } else {
    //                             callbackContext.error(isSuccess + "");
    //                         }
    //                     }

    //                     @Override
    //                     public void onReturnString(String result) {
    //                         callbackContext.success(result);
    //                     }

    //                     @Override
    //                     public void onRaiseException(int code, String msg) {
    //                         callbackContext.error(msg);
    //                     }
    //                 });
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //                 callbackContext.error(e.getMessage());
    //             }
    //         }
    //     });
    // }

    public void sendRAWData(String base64EncriptedData, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final byte[] d = Base64.decode(base64EncriptedData, Base64.DEFAULT);
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printRawData(d, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void setAlignment(int alignment, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final int align = alignment;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.setPrinterPrintAlignment(align, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void setFontName(String typeface, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final String tf = typeface;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.setPrinterPrintFontType(tf, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void setFontSize(int fontsize, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final int fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.setPrinterPrintFontSize(fs, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void printTextWithFont(String text, String typeface, int fontsize, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final String txt = text;
        final String tf = typeface;
        final int fs = fontsize;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printSpecifiedTypeText(txt, tf, fs, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void printColumnsText(JSONArray colsTextArr, JSONArray colsWidthArr, JSONArray colsAlign,
            final CallbackContext callbackContext) {
    
        final IPosPrinterService printerService = iPosPrinterService;
        final String[] clst = new String[colsTextArr.length()];
        for (int i = 0; i < colsTextArr.length(); i++) {
            try {
                clst[i] = colsTextArr.getString(i);
            } catch (JSONException e) {
                
                clst[i] = "-";
                Log.i(TAG, "ERROR TEXT: " + e.getMessage());
            }
        }
        final int[] clsw = new int[colsWidthArr.length()];
        for (int i = 0; i < colsWidthArr.length(); i++) {
            try {
                clsw[i] = colsWidthArr.getInt(i);
            } catch (JSONException e) {
                clsw[i] = 1;
                Log.i(TAG, "ERROR WIDTH: " + e.getMessage());
            }
        }
        final int[] clsa = new int[colsAlign.length()];
        for (int i = 0; i < colsAlign.length(); i++) {
            try {
                clsa[i] = colsAlign.getInt(i);
            } catch (JSONException e) {
                clsa[i] = 0;
                Log.i(TAG, "ERROR ALIGN: " + e.getMessage());
            }
        }
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printColumnsText(clst, clsw, clsa, 0, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    // public void printBitmap(String data, int width, int height, final CallbackContext callbackContext) {
    //     try {
    //         final IPosPrinterService printerService = iPosPrinterService;
    //         byte[] decoded = Base64.decode(data, Base64.DEFAULT);
    //         final Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
    //         ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //             @Override
    //             public void run() {
    //                 try {
    //                     printerService.printBitmap(bitMap, new IPosPrinterCallback.Stub() {
    //                         @Override
    //                         public void onRunResult(boolean isSuccess) {
    //                             if (isSuccess) {
    //                                 callbackContext.success("");
    //                             } else {
    //                                 callbackContext.error(isSuccess + "");
    //                             }
    //                         }

    //                         @Override
    //                         public void onReturnString(String result) {
    //                             callbackContext.success(result);
    //                         }

    //                         @Override
    //                         public void onRaiseException(int code, String msg) {
    //                             callbackContext.error(msg);
    //                         }
    //                     });
    //                 } catch (Exception e) {
    //                     e.printStackTrace();
    //                     Log.i(TAG, "ERROR: " + e.getMessage());
    //                     callbackContext.error(e.getMessage());
    //                 }
    //             }
    //         });
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         Log.i(TAG, "ERROR: " + e.getMessage());
    //     }
    // }

    public void printBarCode(String data, int symbology, int width, int height, int textPosition,
            final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final String d = data;
        final int s = symbology;
        final int h = height;
        final int w = width;
        final int tp = textPosition;

        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printBarCode(d, s, h, w, tp, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void printQRCode(String data, int moduleSize, int errorLevel, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final String d = data;
        final int size = moduleSize;
        final int level = errorLevel;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printQRCode(d, size, level, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void printOriginalText(String text, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final String txt = text;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printText(txt, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    // public void commitPrinterBuffer() {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.commitPrinterBuffer();
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //             }
    //         }
    //     });
    // }

    // public void enterPrinterBuffer(boolean clean) {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     final boolean c = clean;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.enterPrinterBuffer(c);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //             }
    //         }
    //     });
    // }

    // public void exitPrinterBuffer(boolean commit) {
    //     final IPosPrinterService printerService = iPosPrinterService;
    //     final boolean com = commit;
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         @Override
    //         public void run() {
    //             try {
    //                 printerService.exitPrinterBuffer(com);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //                 Log.i(TAG, "ERROR: " + e.getMessage());
    //             }
    //         }
    //     });
    // }

    public void printString(String message, final CallbackContext callbackContext) {
        final IPosPrinterService printerService = iPosPrinterService;
        final String msgs = message;
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.printText(msgs, new IPosPrinterCallback.Stub() {
                        @Override
                        public void onRunResult(boolean isSuccess) {
                            if (isSuccess) {
                                callbackContext.success("");
                            } else {
                                callbackContext.error(isSuccess + "");
                            }
                        }

                        @Override
                        public void onReturnString(String result) {
                            callbackContext.success(result);
                        }

                        @Override
                        public void onRaiseException(int code, String msg) {
                            callbackContext.error(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "ERROR: " + e.getMessage());
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    public void printerStatusStartListener(final CallbackContext callbackContext) {
        final PrinterStatusReceiver receiver = printerStatusReceiver;
        receiver.startReceiving(callbackContext);
    }

    public void printerStatusStopListener() {
        final PrinterStatusReceiver receiver = printerStatusReceiver;
        receiver.stopReceiving();
    }

    // public void printKoubeiBill() {
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {

    //         // @Override
    //         public void run() {
    //             try {
    //                 mIPosPrinterService.printSpecifiedTypeText("   #4口碑外卖\n", "ST", 48, callback);
    //                 mIPosPrinterService.printSpecifiedTypeText(
    //                         "         " + "冯记黄焖鸡米饭\n********************************\n", "ST", 24, callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("17:20 尽快送达\n", "ST", 48, callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("--------------------------------\n", "ST", 24,
    //                         callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("18610858337韦小宝创智天地广场7号楼(605室)\n", "ST", 48, callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("--------------------------------\n", "ST", 24,
    //                         callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("下单: 16:35\n", "ST", 48, callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("********************************\n", "ST", 24,
    //                         callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("菜品          数量   单价   "
    //                         + "金额\n--------------------------------\n黄焖五花肉 (大) (不辣)\n"
    //                         + "               1      25      25\n黄焖五花肉 (小) (不辣)\n               1      "
    //                         + "25      25黄焖五花肉 (小) (微辣)\n               1      25      25\n--------------------------------\n配送费"
    //                         + "  " + "               " + "        2\n--------------------------------\n", "ST", 24,
    //                         callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("            实付金额: 27\n\n", "ST", 32, callback);
    //                 mIPosPrinterService.printSpecifiedTypeText("    口碑外卖\n\n\n", "ST", 48, callback);

    //                 mIPosPrinterService.printerPerformPrint(160, callback);
    //             } catch (RemoteException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     });
    // }

    // public void loopPrint(int flag) {
    //     switch (flag) {
    //     case INPUT_CONTENT_LOOP_PRINT:
    //         bigDataPrintTest(127, loopContent);
    //         break;
    //     case PRINT_DRIVER_ERROR_TEST:
    //         // printDriverTest();
    //         break;
    //     default:
    //         break;
    //     }
    // }

    // public void bigDataPrintTest(final int numK, final byte data) {
    //     ThreadPoolManager.getInstance().executeTask(new Runnable() {
    //         // @Override
    //         public void run() {
    //             int num4K = 1024 * 4;
    //             int length = numK > 127 ? num4K * 127 : num4K * numK;
    //             byte[] dataBytes = new byte[length];
    //             for (int i = 0; i < length; i++) {
    //                 dataBytes[i] = data;
    //             }
    //             try {
    //                 mIPosPrinterService.printRawData(dataBytes, callback);
    //                 mIPosPrinterService.printerPerformPrint(160, callback);
    //             } catch (RemoteException e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     });
    // }

}