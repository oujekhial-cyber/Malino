import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
Dialog{property var editing:null;modal:true;width:Math.min(root.width-24,390);title:editing?"ویرایش تراکنش":"ثبت تراکنش";contentItem:TransactionEditor{editing:parent.editing;onSaved:parent.close()}}
