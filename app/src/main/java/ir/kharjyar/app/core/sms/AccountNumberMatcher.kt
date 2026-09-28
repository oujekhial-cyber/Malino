package ir.kharjyar.app.core.sms

import ir.kharjyar.app.core.text.Digits

/** اطلاعات عددی حساب که برای تطبیق پیامک لازم است. */
data class MatchableAccount(val id:Long,val maskedNumber:String,val accountNumber:String,val iban:String,val cardNumber:String)

/** تطبیق حساب از شماره کارت/حساب/شبا، حتی وقتی فقط بخشی از میانه شناسه در SMS آمده است. */
object AccountNumberMatcher {
    fun match(body:String,accounts:List<MatchableAccount>):AccountMatch {
        val tokens=numericTokens(body)
        val scores=accounts.mapNotNull{account->
            val plainScore=identifiers(account).maxOfOrNull{saved->tokens.maxOfOrNull{token->agreementScore(token,saved)}?:0}?:0
            val score=maxOf(plainScore,maskedEvidenceScore(body,account))
            if(score>=4)account.id to score else null
        }
        val best=scores.maxOfOrNull{it.second}?:return AccountMatch.Unknown
        val matched=scores.filter{it.second==best}.map{it.first}.distinct()
        return when(matched.size){0->AccountMatch.Unknown;1->AccountMatch.Single(matched.single());else->AccountMatch.Ambiguous(matched)}
    }

    private fun identifiers(account:MatchableAccount):Set<String> =
        listOf(account.maskedNumber,account.accountNumber,account.iban,account.cardNumber)
            .map{Digits.normalize(it).filter(Char::isDigit)}
            .filter{it.length>=4}.toSet()

    private fun numericTokens(text:String):List<String> =
        Regex("[\\d۰-۹٠-٩*٭][\\d۰-۹٠-٩*٭.\\-/\\\\]{2,}[\\d۰-۹٠-٩]").findAll(text)
            .map{Digits.normalize(it.value).filter(Char::isDigit)}
            .filter{it.length>=4}.toList()

    /**
     * تطبیق دیگر به انتهای حساب وابسته نیست. قوی‌ترین قطعه پیوسته ۶، سپس ۵ و
     * سپس ۴ رقمی در هر جای دو شناسه جست‌وجو می‌شود. جداکننده‌های رایج پیش از
     * مقایسه حذف می‌شوند، بنابراین 2404.306 یا 2404-306 نیز قابل تشخیص‌اند.
     * تطبیق کامل امتیاز بالاتری دارد تا بر قطعه کوتاه و تصادفی مقدم باشد.
     */
    private fun agreementScore(inMessage:String,saved:String):Int {
        if(inMessage==saved)return 100+saved.length
        for(length in 6 downTo 4){
            if(inMessage.length<length||saved.length<length)continue
            val fragments=(0..inMessage.length-length).asSequence().map{inMessage.substring(it,it+length)}
            if(fragments.any{saved.contains(it)})return length
        }
        return 0
    }

    /** پیشوند و پسوند دو سوی ستاره‌های ماسک‌شده باید در همان شناسه ذخیره‌شده باشند. */
    private fun maskedEvidenceScore(body:String,account:MatchableAccount):Int {
        val groups=Regex("[\\d۰-۹٠-٩]{4,}(?:[*٭]+[\\d۰-۹٠-٩]{4,})+").findAll(body)
        val saved=identifiers(account)
        return groups.maxOfOrNull { match ->
            val pieces=Regex("[\\d۰-۹٠-٩]{4,}").findAll(match.value)
                .map{Digits.normalize(it.value).filter(Char::isDigit)}.toList()
            if(pieces.size>=2 && saved.any{identifier->
                var cursor=0
                pieces.all{piece->identifier.indexOf(piece,cursor).also{if(it>=0)cursor=it+piece.length}>=0}
            }) 50+pieces.sumOf{it.length} else 0
        }?:0
    }
}
