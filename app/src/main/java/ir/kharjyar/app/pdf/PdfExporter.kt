package ir.kharjyar.app.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import ir.kharjyar.app.R
import ir.kharjyar.app.core.text.Digits
import java.io.OutputStream

/** سازنده گزارش PDF فارسی، راست‌به‌چپ و جدول‌محور. */
object PdfExporter {
    data class ReportRow(val dateText:String,val accountTitle:String,val description:String,val categoryName:String,val natureText:String,val amountText:String)
    data class ReportData(val title:String,val rangeText:String,val filtersText:String,val incomeText:String,val expenseText:String,val netText:String,val pendingNote:String?,val incomeSeries:List<Long>,val expenseSeries:List<Long>,val rows:List<ReportRow>)

    private const val PAGE_W=595
    private const val PAGE_H=842
    private const val MARGIN=32f
    private const val TABLE_W=531f
    private val BLUE=Color.rgb(35,85,170)
    private val GREEN=Color.rgb(23,135,84)
    private val RED=Color.rgb(204,56,67)
    private val INK=Color.rgb(31,41,55)
    private val MUTED=Color.rgb(92,102,117)

    fun export(context:Context,data:ReportData,out:OutputStream){val regular=ResourcesCompat.getFont(context,R.font.vazirmatn_regular)?:Typeface.DEFAULT;val bold=ResourcesCompat.getFont(context,R.font.vazirmatn_bold)?:Typeface.DEFAULT_BOLD;val doc=PdfDocument();var number=1;var page=doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,number).create());var canvas=page.canvas;var y=MARGIN
        fun footer(){canvas.drawLine(MARGIN,PAGE_H-32f,PAGE_W-MARGIN,PAGE_H-32f,Paint().apply{color=0xFFE2E8F0.toInt()});drawTextBox(canvas,"خرج‌یار  •  گزارش مالی شخصی  •  صفحه ${Digits.toPersian(number.toString())}",regular,8.5f,MARGIN,PAGE_H-27f,TABLE_W,18f,Color.GRAY,Layout.Alignment.ALIGN_CENTER)}
        fun nextPage(){footer();doc.finishPage(page);number++;page=doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W,PAGE_H,number).create());canvas=page.canvas;y=MARGIN;drawTextBox(canvas,"ادامه جدول تراکنش‌ها",bold,13f,MARGIN,y,TABLE_W,24f,INK,Layout.Alignment.ALIGN_NORMAL);y+=30f;y=drawTableHeader(canvas,y,bold)}
        // Header band
        canvas.drawRoundRect(MARGIN,y,PAGE_W-MARGIN,y+62f,12f,12f,Paint(Paint.ANTI_ALIAS_FLAG).apply{color=BLUE});drawTextBox(canvas,data.title,bold,20f,MARGIN+14,y+9,TABLE_W-28,27f,Color.WHITE,Layout.Alignment.ALIGN_NORMAL);drawTextBox(canvas,data.rangeText,regular,10f,MARGIN+14,y+37,TABLE_W-28,18f,0xFFDCE7FF.toInt(),Layout.Alignment.ALIGN_NORMAL);y+=74f
        if(data.filtersText.isNotBlank()){canvas.drawRoundRect(MARGIN,y,PAGE_W-MARGIN,y+29f,7f,7f,Paint().apply{color=0xFFF1F5F9.toInt()});drawTextBox(canvas,"فیلترها: ${data.filtersText}",regular,9.5f,MARGIN+9,y+6,TABLE_W-18,18f,MUTED,Layout.Alignment.ALIGN_NORMAL);y+=37f}
        // Three summary cards
        val gap=8f;val card=(TABLE_W-gap*2)/3f;summaryCard(canvas,MARGIN,y,card,"خالص",data.netText,BLUE,regular,bold);summaryCard(canvas,MARGIN+card+gap,y,card,"برداشت",data.expenseText,RED,regular,bold);summaryCard(canvas,MARGIN+(card+gap)*2,y,card,"واریز",data.incomeText,GREEN,regular,bold);y+=65f
        data.pendingNote?.let{canvas.drawRoundRect(MARGIN,y,PAGE_W-MARGIN,y+31f,7f,7f,Paint().apply{color=0xFFFFF4DE.toInt()});drawTextBox(canvas,it,regular,9f,MARGIN+9,y+6,TABLE_W-18,20f,0xFF8A5A00.toInt(),Layout.Alignment.ALIGN_NORMAL);y+=39f}
        drawTextBox(canvas,"جزئیات تراکنش‌ها  (${Digits.toPersian(data.rows.size.toString())} مورد)",bold,13f,MARGIN,y,TABLE_W,25f,INK,Layout.Alignment.ALIGN_NORMAL);y+=31f;y=drawTableHeader(canvas,y,bold)
        if(data.rows.isEmpty()){canvas.drawRect(MARGIN,y,PAGE_W-MARGIN,y+55f,Paint().apply{color=0xFFF8FAFC.toInt()});drawTextBox(canvas,"در بازه و فیلتر انتخاب‌شده تراکنشی وجود ندارد.",regular,10f,MARGIN,y+17,TABLE_W,22f,MUTED,Layout.Alignment.ALIGN_CENTER);y+=55f}else data.rows.forEachIndexed{index,row->val height=if(row.description.length>45)58f else 46f;if(y+height>PAGE_H-48f)nextPage();drawTableRow(canvas,y,height,row,index%2==0,regular);y+=height}
        footer();doc.finishPage(page);doc.writeTo(out);doc.close()
    }

    private fun summaryCard(canvas:Canvas,left:Float,top:Float,width:Float,label:String,value:String,color:Int,regular:Typeface,bold:Typeface){canvas.drawRoundRect(left,top,left+width,top+55f,9f,9f,Paint(Paint.ANTI_ALIAS_FLAG).apply{this.color=color;alpha=24});canvas.drawRect(left+width-4f,top+8f,left+width,top+47f,Paint().apply{this.color=color});drawTextBox(canvas,label,regular,9f,left+9,top+7,width-18,16f,MUTED,Layout.Alignment.ALIGN_NORMAL);drawTextBox(canvas,value,bold,11f,left+9,top+26,width-18,22f,color,Layout.Alignment.ALIGN_NORMAL)}

    /** ستون‌ها از چپ: مبلغ، شرح، دسته، نوع، حساب، تاریخ؛ در PDF RTL تاریخ در سمت راست دیده می‌شود. */
    // ستون حساب عریض‌تر است تا عنوان و شماره حساب کامل در دو خط قابل تشخیص باشد.
    private val widths=floatArrayOf(88f,120f,67f,52f,110f,94f)
    private val headers=arrayOf("مبلغ","شرح","دسته","نوع","حساب","تاریخ و ساعت")
    private fun drawTableHeader(canvas:Canvas,top:Float,bold:Typeface):Float{var x=MARGIN;headers.forEachIndexed{i,text->canvas.drawRect(x,top,x+widths[i],top+31f,Paint().apply{color=BLUE});drawTextBox(canvas,text,bold,8.5f,x+3,top+7,widths[i]-6,18f,Color.WHITE,Layout.Alignment.ALIGN_CENTER);x+=widths[i]};return top+31f}
    private fun drawTableRow(canvas:Canvas,top:Float,height:Float,row:ReportRow,even:Boolean,font:Typeface){val bg=if(even)0xFFF8FAFC.toInt() else Color.WHITE;canvas.drawRect(MARGIN,top,MARGIN+TABLE_W,top+height,Paint().apply{color=bg});val values=arrayOf(row.amountText,row.description.ifBlank{"—"},row.categoryName.ifBlank{"بدون دسته"},row.natureText,row.accountTitle,row.dateText);var x=MARGIN;values.forEachIndexed{i,text->val tint=when{i==0&&row.natureText.contains("واریز")->GREEN;i==0&&row.natureText.contains("برداشت")->RED;else->INK};drawTextBox(canvas,text,font,if(i==1)8.2f else 8f,x+4,top+6,widths[i]-8,height-12,tint,if(i==1)Layout.Alignment.ALIGN_NORMAL else Layout.Alignment.ALIGN_CENTER,2);canvas.drawLine(x,top,x,top+height,Paint().apply{color=0xFFE2E8F0.toInt();strokeWidth=.6f});x+=widths[i]};canvas.drawLine(MARGIN,top+height,MARGIN+TABLE_W,top+height,Paint().apply{color=0xFFE2E8F0.toInt();strokeWidth=.7f});canvas.drawLine(MARGIN+TABLE_W,top,MARGIN+TABLE_W,top+height,Paint().apply{color=0xFFE2E8F0.toInt();strokeWidth=.6f})}

    private fun drawTextBox(canvas:Canvas,text:String,typeface:Typeface,size:Float,left:Float,top:Float,width:Float,height:Float,color:Int,alignment:Layout.Alignment,maxLines:Int=1){val paint=TextPaint(Paint.ANTI_ALIAS_FLAG).apply{this.typeface=typeface;textSize=size;this.color=color};val layout=StaticLayout.Builder.obtain(text,0,text.length,paint,width.toInt().coerceAtLeast(1)).setAlignment(alignment).setTextDirection(android.text.TextDirectionHeuristics.RTL).setMaxLines(maxLines).setEllipsize(android.text.TextUtils.TruncateAt.END).build();canvas.save();canvas.clipRect(left,top,left+width,top+height);canvas.translate(left,top+(height-layout.height).coerceAtLeast(0f)/2f);layout.draw(canvas);canvas.restore()}
}
