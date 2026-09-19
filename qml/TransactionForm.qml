import QtQuick
import QtQuick.Controls
import QtQuick.Layouts
Dialog{
    id: root
    property var editing: null
    modal:true
    width:Math.min(root.parent ? root.parent.width : 360, 390)
    title:root.editing?"ویرایش تراکنش":"ثبت تراکنش"
    contentItem:TransactionEditor{editing:root.editing;onSaved:root.close()}
}
