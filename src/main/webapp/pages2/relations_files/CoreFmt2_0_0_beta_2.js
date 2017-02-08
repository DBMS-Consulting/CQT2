var _byteLenKey="org.apache.myfaces.trinidad.validator.ByteLengthValidator.MAXIMUM";
function TrByteLengthValidator(
a0,
a1
)
{
this._length=a0;
this._messages=a1;
this._class="TrByteLengthValidator";
}
TrByteLengthValidator.prototype=new TrValidator();
function CjkFormat(
a0,
a1
)
{
this._base=TrByteLengthValidator;
this._base(a0,a1);
this._class="CjkFormat";
}
CjkFormat.prototype=new TrByteLengthValidator();
CjkFormat.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._length)
);
}
return a3;
}
CjkFormat.prototype.validate=function(
a4,
a5,
a6
)
{
var a7=0;
var a8=this._length;
if(a4==null)
return;
while(a7<a4.length)
{
var a9=a4.charCodeAt(a7);
if((a9<0x80)||((0xFF60<a9)&&(a9<0xFFA0)))a8--;
else a8-=2;
if(a8<0)
{
var a10;
if(!this._messages["detail"])
{
a10=_createFacesMessage(_byteLenKey,
a5,
a4,
this._length);
}
else
{
a10=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(_byteLenKey),
this._messages["detail"],
a5,
a4,
this._length);
}
throw new TrValidatorException(a10);
}
a7++;
}
return a4;
}
function Utf8Format(
a0,
a1
)
{
this._base=TrByteLengthValidator;
this._base(a0,a1);
this._class="Utf8Format";
}
Utf8Format.prototype=new TrByteLengthValidator();
Utf8Format.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._length)
);
}
return a3;
}
Utf8Format.prototype.validate=function(
a4,
a5,
a6
)
{
var a7=0;
var a8=this._length;
if(a4==null)
return null;
while(a7<a4.length)
{
var a9=a4.charCodeAt(a7);
if(a9<0x80)a8--;
else if(a9<0x800)a8-=2;
else
{
if((a9&0xF800)==0xD800)
a8-=2;
else
a8-=3;
}
if(a8<0)
{
var a10;
if(!this._messages["detail"])
{
a10=_createFacesMessage(_byteLenKey,
a5,
a4,
this._length);
}
else
{
a10=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(_byteLenKey),
this._messages["detail"],
a5,
a4,
this._length);
}
throw new TrValidatorException(a10);
}
a7++;
}
return a4;
}
function SBFormat(
a0,
a1
)
{
this._base=TrByteLengthValidator;
this._base(a0,a1);
this._class="SBFormat";
}
SBFormat.prototype=new TrByteLengthValidator();
SBFormat.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
this._length)
);
}
return a3;
}
SBFormat.prototype.validate=function(
a4,
a5,
a6
)
{
if(a4==null)
return null;
if(this._length<a4.length)
{
var a7;
if(!this._messages["detail"])
{
a7=_createFacesMessage(_byteLenKey,
a5,
a4,
this._length);
}
else
{
a7=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(_byteLenKey),
this._messages["detail"],
a5,
a4,
this._length);
}
throw new TrValidatorException(a7);
}
return a4;
}

var TrCollections=new Object();
TrCollections.removeValuesFromArray=function(
a0,
a1
)
{
if(a0&&a1)
{
for(i=0;i<a0.length;i++)
{
var a2=a0[i];
for(j=0;j<a1.length;j++)
{
if(a1[j].toLowerCase()==a2.toLowerCase())
{
a1.splice(j,1);
j--;
}
}
}
}
}

function TrNumberFormat(a0,a1,a2)
{
if(!a0)
alert("type for TrNumberFormat not defined!");
this._type=a0;
if(!a2)
{
a2={};
}
TrNumberFormat.CURRENCY_CODE="currencyCode";
TrNumberFormat.CURRENCY_SYMBOL="currencySymbol";
TrNumberFormat.NEGATIVE_PREFIX="negativePrefix";
TrNumberFormat.NEGATIVE_SUFFIX="negativeSuffix";
TrNumberFormat.IS_GROUPING_USED="isGroupingUsed";
TrNumberFormat.MAX_FRACTION_DIGITS="maxFractionDigits";
TrNumberFormat.MAX_INTEGER_DIGITS="maxIntegerDigits";
TrNumberFormat.MIN_FRACTION_DIGITS="minFractionDigits";
TrNumberFormat.MIN_INTEGER_DIGITS="minIntegerDigits";
TrNumberFormat.ROUNDING_MODE="roundingMode";
if(this._type=="percent")
{
this.setMaximumFractionDigits((a2[TrNumberFormat.MAX_FRACTION_DIGITS]!=null)?a2[TrNumberFormat.MAX_FRACTION_DIGITS]:0);
}
else
{
this.setMaximumFractionDigits((a2[TrNumberFormat.MAX_FRACTION_DIGITS]!=null)?a2[TrNumberFormat.MAX_FRACTION_DIGITS]:3);
}
this.setMaximumIntegerDigits((a2[TrNumberFormat.MAX_INTEGER_DIGITS]!=null)?a2[TrNumberFormat.MAX_INTEGER_DIGITS]:40);
if(this._type=="currency")
{
this.setMinimumFractionDigits((a2[TrNumberFormat.MIN_FRACTION_DIGITS]!=null)?a2[TrNumberFormat.MIN_FRACTION_DIGITS]:2);
}
else
{
this.setMinimumFractionDigits((a2[TrNumberFormat.MIN_FRACTION_DIGITS]!=null)?a2[TrNumberFormat.MIN_FRACTION_DIGITS]:0);
}
this.setMinimumIntegerDigits((a2[TrNumberFormat.MIN_INTEGER_DIGITS]!=null)?a2[TrNumberFormat.MIN_INTEGER_DIGITS]:1);
this.setGroupingUsed((a2[TrNumberFormat.IS_GROUPING_USED]!=null)?a2[TrNumberFormat.IS_GROUPING_USED]:true);
this.setRoundingMode(a2[TrNumberFormat.ROUNDING_MODE]);
this._updateLocaleAndSymbols(a1,a2);
}
TrNumberFormat.getNumberInstance=function(a3,a4)
{
return new TrNumberFormat("number",a3,a4);
}
TrNumberFormat.getCurrencyInstance=function(a5,a6)
{
return new TrNumberFormat("currency",a5,a6);
}
TrNumberFormat.getPercentInstance=function(a7,a8)
{
return new TrNumberFormat("percent",a7,a8);
}
TrNumberFormat.prototype.setGroupingUsed=function(a9)
{
this._groupingUsed=a9;
}
TrNumberFormat.prototype.isGroupingUsed=function()
{
return this._groupingUsed;
}
TrNumberFormat.prototype.setRoundingMode=function(a10)
{
this._roundingMode=a10;
}
TrNumberFormat.prototype.getRoundingMode=function()
{
return this._roundingMode;
}
TrNumberFormat.prototype.isRoundingModeSpecified=function()
{
return this.getRoundingMode()!=null;
}
TrNumberFormat.prototype.setMaximumIntegerDigits=function(a11)
{
if(a11!=null)
{
this._maxIntegerDigits=a11<0?0:a11;
if(this._minIntegerDigits>this._maxIntegerDigits)
{
this._minIntegerDigits=this._maxIntegerDigits;
}
}
}
TrNumberFormat.prototype.getMaximumIntegerDigits=function()
{
return this._maxIntegerDigits;
}
TrNumberFormat.prototype.setMaximumFractionDigits=function(a12)
{
if(a12!=null)
{
this._maxFractionDigits=a12<0?0:a12;
if(this._maxFractionDigits<this._minFractionDigits)
{
this._minFractionDigits=this._maxFractionDigits;
}
this._isMaxFractionDigitsSet=true;
}
}
TrNumberFormat.prototype.getMaximumFractionDigits=function()
{
return this._maxFractionDigits;
}
TrNumberFormat.prototype.setMinimumIntegerDigits=function(a13)
{
if(a13!=null)
{
this._minIntegerDigits=a13<0?0:a13;
if(this._minIntegerDigits>this._maxIntegerDigits)
{
this._maxIntegerDigits=this._minIntegerDigits;
}
}
}
TrNumberFormat.prototype.getMinimumIntegerDigits=function()
{
return this._minIntegerDigits;
}
TrNumberFormat.prototype.setMinimumFractionDigits=function(a14)
{
if(a14!=null)
{
this._minFractionDigits=a14<0?0:a14;
if(this._maxFractionDigits<this._minFractionDigits)
{
this._maxFractionDigits=this._minFractionDigits;
}
}
}
TrNumberFormat.prototype.getMinimumFractionDigits=function()
{
return this._minFractionDigits;
}
TrNumberFormat.prototype.format=function(a15)
{
if(this._type=="percent")
return this.percentageToString(a15);
else if(this._type=="currency")
return this.currencyToString(a15);
else if(this._type=="number")
return this.numberToString(a15);
}
TrNumberFormat.prototype.parse=function(a16)
{
if(this._type=="percent")
return this.stringToPercentage(a16);
else if(this._type=="currency")
return this.stringToCurrency(a16);
return this.stringToNumber(a16);
}
TrNumberFormat.prototype.stringToNumber=function(a17)
{
var a18=this.hasPrefixOrSuffix(a17);
var a19=1;
if(a18)
{
var a20=this.removePrefixAndSuffix(a17);
a17=a20[0]
var a21=a20[1];
if(!a21)
a19=-1;
}
if(isNaN(a17)||a17.indexOf('e')!=-1||a17.indexOf('E')!=-1)
{
throw new TrParseException("not able to parse number");
}
return parseFloat(a17)*a19;
}
TrNumberFormat.prototype.hasPrefixOrSuffix=function(a22)
{
var a23=a22.indexOf(this._nPrefix);
var a24=this._nSuffix;
if(a24&&(a24.charAt(0)==' '||a24.charAt(0)=='\xa0'))
{
a24=a24.substring(1);
}
var a25=a22.indexOf((a24=="")?null:a24);
if(a23==0||a25!=-1)
{
return true;
}
else
{
var a26=a22.indexOf(this._pPrefix);
var a27=this._pSuffix;
if(a27&&(a27.charAt(0)==' '||a27.charAt(0)=='\xa0'))
{
a27=a27.substring(1);
}
var a28=a22.indexOf((a27=="")?null:a27);
if(a26==0||a28!=-1)
{
return true;
}
}
return false;
}
TrNumberFormat.prototype.stringToCurrency=function(a29)
{
return this.stringToNumber(a29);
}
TrNumberFormat.prototype.removePrefixAndSuffix=function(a30)
{
var a31=[];
var a32=a30.indexOf(this._nPrefix);
var a33=this._nSuffix;
if(a33&&(a33.charAt(0)==' '||a33.charAt(0)=='\xa0'))
{
a33=a33.substring(1);
}
var a34=a30.indexOf(a33);
if(a32!=-1&&a34!=-1)
{
a31.push(a30.substr(this._nPrefix.length,a30.length-(this._nPrefix.length+a33.length)));
a31.push(false);
return a31;
}
else
{
var a35=a30.indexOf(this._pPrefix);
var a36=this._pSuffix;
if(a36&&(a36.charAt(0)==' '||a36.charAt(0)=='\xa0'))
{
a36=a36.substring(1);
}
var a37=a30.indexOf(a36);
if(a35!=-1&&a37!=-1)
{
a31.push(a30.substr(this._pPrefix.length,a30.length-(this._pPrefix.length+a36.length)));
a31.push(true);
return a31;
}
else
{
throw new TrParseException("not able to parse number");
}
}
}
TrNumberFormat.prototype.stringToPercentage=function(a38)
{
var a39=this._localeSymbols.getPercentSuffix().trim();
if(!a39)
a39=this._localeSymbols.getPercentPrefix().trim();
var a40=(a38.indexOf(a39)!=-1);
if(!a40)
{
throw new TrParseException("not able to parse number");
}
var a41=a38.replace(new RegExp(a39,'g'),'');
return this.stringToNumber(a41);
}
TrNumberFormat.prototype.numberToString=function(a42,a43,a44)
{
var a45=a42<0;
if(a45)
a42=(a42*-1);
var a46=a42+"";
a46=TrNumberFormat.scientificToExpanded(a46);
var a47=a46.indexOf(".");
var a48=a46.length;
var a49;
var a50;
if(a47!=-1)
{
a49=a46.substring(0,a47);
a50=a46.substring(a47+1,a48);
}
else
{
a49=a46;
a50="";
}
a49=this._formatIntegers(a49);
a50=this._formatFractions(a50)
var a51=this._localeSymbols.getDecimalSeparator();
if(a50!="")
a46=(a49+a51+a50);
else
a46=(a49);
if(a43)
{
a46=a46+a43;
}
else if(a44)
{
a46=a44+a46;
}
if(a45)
{
if(this._nPrefix||this._nSuffix)
{
a46=this.addPrefixAndSuffix(a46,false);
}
else
{
a46="-"+a46;
}
}
else
{
if(this._pPrefix||this._pSuffix)
{
a46=this.addPrefixAndSuffix(a46,true);
}
}
return a46;
}
TrNumberFormat.prototype.addPrefixAndSuffix=function(a52,a53)
{
if(a53)
{
return((this._pPrefix)?this._pPrefix:'')+a52+((this._pSuffix)?this._pSuffix:'');
}
else
{
return((this._nPrefix)?this._nPrefix:'')+a52+((this._nSuffix)?this._nSuffix:'');
}
}
TrNumberFormat.prototype.currencyToString=function(a54)
{
return this.numberToString(a54);
}
TrNumberFormat.prototype.percentageToString=function(a55)
{
a55=this.moveDecimalRight(a55);
if((this._isMaxFractionDigitsSet==null||(this._isMaxFractionDigitsSet&&this.getMaximumFractionDigits()==0))&&
!this.isRoundingModeSpecified())
a55=this.getRounded(a55);
if(isNaN(a55))
{
throw new TrParseException("not able to parse number");
}
var a56=this._localeSymbols.getPercentSuffix();
var a57=this._localeSymbols.getPercentPrefix();
if((!a56||a56=="")&&(!a57||a57==""))
{
throw new TrParseException("suffix AND prefix are undefined, require at least one to be not null");
}
return this.numberToString(a55,a56,a57);
}
TrNumberFormat.scientificToExpanded=function(a58)
{
var a59=a58.indexOf('e');
if(a59==-1)
return a58;
var a60="";
if(a58.charAt(0)=='-')
{
a60="-";
a58=a58.substring(1);
a59-=1;
}
var a61=a58.charAt(a59+1)=='+';
var a62=parseInt(a58.substring(a59+2));
var a63=a59-2;
var a64="";
if(a61)
{
for(var a65=0;a65<a62-a63;++a65)
a64+="0";
return a60+a58.charAt(0)+a58.substring(2,a59)+a64;
}
for(var a65=0;a65<a62-1;++a65)
a64+="0";
return a60+"0."+a64+a58.charAt(0)+a58.substring(2,a59);
}
TrNumberFormat.trimLeadingZeroes=function(a66)
{
var a67=[];
var a68,ch;
for(a68=0;a68<a66.length;++a68)
{
ch=a66.charAt(a68);
if((ch>='1'&&ch<='9')||ch=='.')
break;
if(ch=='0'&&a68+1<a66.length&&a66.charAt(a68+1)!='.')
continue;
a67.push(ch);
}
return a67.join('')+a66.substring(a68);
}
TrNumberFormat.prototype.getRounded=function(a69)
{
a69=this.moveDecimalRight(a69);
a69=Math.round(a69);
a69=this.moveDecimalLeft(a69);
return a69;
}
TrNumberFormat.prototype.moveDecimalRight=function(a70)
{
var a71='';
a71=this.moveDecimal(a70,false);
return a71;
}
TrNumberFormat.prototype.moveDecimalLeft=function(a72)
{
var a73='';
a73=this.moveDecimal(a72,true);
return a73;
}
TrNumberFormat.prototype.moveDecimal=function(a74,a75)
{
var a76='';
a76=this.moveDecimalAsString(a74,a75);
return parseFloat(a76);
}
TrNumberFormat.prototype.moveDecimalAsString=function(a77,a78)
{
var a79=2;
if(a79<=0)
return a77;
var a80=a77+'';
var a81=this.getZeros(a79);
var a82=new RegExp('([0-9.]+)');
if(a78)
{
a80=a80.replace(a82,a81+'$1');
var a83=new RegExp('(-?)([0-9]*)([0-9]{'+a79+'})(\\.?)');
a80=a80.replace(a83,'$1$2.$3');
}
else
{
var a84=a82.exec(a80);
if(a84!=null)
{
a80=a80.substring(0,a84.index)+a84[1]+a81+a80.substring(a84.index+a84[0].length);
}
var a83=new RegExp('(-?)([0-9]*)(\\.?)([0-9]{'+a79+'})');
a80=a80.replace(a83,'$1$2$4.');
}
a80=a80.replace(/\.$/,'');
return a80;
}
TrNumberFormat.prototype.getZeros=function(a85)
{
var a86='';
var a87;
for(a87=0;a87<a85;a87++){
a86+='0';
}
return a86;
}
TrNumberFormat.prototype._updateLocaleAndSymbols=function(a88,a89)
{
var a90=(a89)?a89[TrNumberFormat.CURRENCY_CODE]:null;
var a91=(a89)?a89[TrNumberFormat.CURRENCY_SYMBOL]:null;
var a92=(a89)?a89[TrNumberFormat.NEGATIVE_PREFIX]:null;
var a93=(a89)?a89[TrNumberFormat.NEGATIVE_SUFFIX]:null;
this._localeSymbols=getLocaleSymbols(a88);
if(this._type=="percent"||this._type=="number")
{
this._nPrefix=(a92)?a92:null;
this._nSuffix=(a93)?a93:null;
this._pPrefix=null;;
this._pSuffix=null;
}
else
{
this._pPrefix=this._localeSymbols.getPositivePrefix();
this._pSuffix=this._localeSymbols.getPositiveSuffix();
var a94=this._localeSymbols.getNegativePrefix();
var a95=this._localeSymbols.getNegativeSuffix();
var a96=this._localeSymbols.getCurrencySymbol();
this._nPrefix=this._replaceFormattingPrefixAndSuffix(a94,a96,a92,true);
this._nSuffix=this._replaceFormattingPrefixAndSuffix(a95,a96,a93,false);
if(a90)
{
var a97=this._localeSymbols.getCurrencyCode();
if(a97!=a90)
{
this._replaceCurrencyPrefixAndSuffix(a96,a90);
}
}
else if(a91)
{
this._replaceCurrencyPrefixAndSuffix(a96,a91);
}
}
}
TrNumberFormat.prototype._replaceCurrencyPrefixAndSuffix=function(a98,a99)
{
this._pPrefix=this._pPrefix.replace(a98,a99);
this._pSuffix=this._pSuffix.replace(a98,a99);
this._nPrefix=this._nPrefix.replace(a98,a99);
this._nSuffix=this._nSuffix.replace(a98,a99);
}
TrNumberFormat.prototype._replaceFormattingPrefixAndSuffix=function(
a100,
a101,
a102,
a103)
{
var a104=a100;
if(a102)
{
var a105=a100.trim().indexOf(a101);
if(a105==-1)
{
a104=a102;
}
else
{
if(a100.trim()==a101)
{
a104=(a103)?a102+a100:a100+a102;
}
else if(a105==0)
{
var a106=a100.indexOf(a101);
a104=a100.substring(0,a106)+(a101+a102);
}
else
{
var a107=a101.length;
if(a100.trim().length==a107+a105)
{
var a106=a100.indexOf(a101);
a104=(a102+a101)+a100.substring(a106+a107,a100.length);
}
else
{
a104=a102+a101;
}
}
}
}
return a104;
}
TrNumberFormat.prototype._formatIntegers=function(a108)
{
var a109=a108.length;
var a110=this.getMaximumIntegerDigits();
var a111=this.getMinimumIntegerDigits();
var a112;
if(a109>a110)
{
a112=a109-a110;
a108=a108.substring(a112,a109);
}
else if(a109<a111)
{
a112=a111-a109;
var a113="";
while(a112>0)
{
a113="0"+a113;
--a112;
}
a108=a113+a108;
}
if(this.isGroupingUsed())
{
a108=this._addGroupingSeparators(a108);
}
return a108;
}
TrNumberFormat.prototype._formatFractions=function(a114)
{
var a115=a114.length;
var a116=this.getMaximumFractionDigits();
var a117=this.getMinimumFractionDigits();
if(a115>a116&&a116>=a117)
{
var a118=(this.isRoundingModeSpecified())?a115:a116;
a114=a114.substring(0,a118);
}
if(a115<a117)
{
var a119=a117-a115;
while(a119>0)
{
a114=a114+"0";
--a119;
}
}
return a114;
}
TrNumberFormat.prototype._addGroupingSeparators=function(a120)
{
var a121=a120.length;
var a122=a121%3;
var a123;
var a124;
var a125="";
var a126=this._localeSymbols.getGroupingSeparator();
if(a122>0)
{
a123=(a121<4)?a120.substring(0,a122):a120.substring(0,a122)+a126;
a124=a120.substring(a122,a121);
}
else
{
a123="";
a124=a120;
}
for(i=0;i<a124.length;i++)
{
if(i%3==0&&i!=0)
{
a125+=a126;
}
a125+=a124.charAt(i);
}
a120=a123+a125;
return a120;
}
function TrParseException(
a0
)
{
this._message=a0;
}
TrParseException.prototype.getMessage=function()
{
return this._message;
}

function TrNumberConverter(
a0,
a1,
a2,
a3,
a4,
a5,
a6,
a7,
a8,
a9,
a10,
a11,
a12,
a13,
a14,
a15)
{
this._pattern=a0;
this._type=a1;
this._locale=a2;
this._messages=a3;
this._isDisabled=a4;
this._currencyCode=a7;
this._currencySymbol=a8;
this._maxFractionDigits=a9;
this._maxIntegerDigits=a10;
this._minFractionDigits=a11;
this._minIntegerDigits=a12;
this._negativePrefix=a13;
this._negativeSuffix=a14;
this._roundingMode=a15;
if(a5!==undefined)
this._integerOnly=a5;
else
this._integerOnly=false;
if(a6!==undefined)
this._groupingUsed=a6;
else
this._groupingUsed=true;
this._initNumberFormat(a2,a7,a8,a13,a14);
this._class="TrNumberConverter";
TrNumberConverter.ROUND_UP="UP";
TrNumberConverter.ROUND_DOWN="DOWN";
TrNumberConverter.ROUND_CEILING="CEILING";
TrNumberConverter.ROUND_FLOOR="FLOOR";
TrNumberConverter.ROUND_HALF_UP="HALF_UP";
TrNumberConverter.ROUND_HALF_DOWN="HALF_DOWN";
TrNumberConverter.ROUND_HALF_EVEN="HALF_EVEN";
TrNumberConverter.ROUND_UNNECESSARY="UNNECESSARY";
}
TrNumberConverter.prototype=new TrConverter();
TrNumberConverter.prototype.setCurrencyCode=function(a16)
{
this._currencyCode=a16;
}
TrNumberConverter.prototype.getCurrencyCode=function()
{
return this._currencyCode;
}
TrNumberConverter.prototype.setCurrencySymbol=function(a17)
{
this._currencySymbol=a17;
}
TrNumberConverter.prototype.getCurrencySymbol=function()
{
return this._currencySymbol;
}
TrNumberConverter.prototype.setMaxFractionDigits=function(a18)
{
this._maxFractionDigits=a18;
}
TrNumberConverter.prototype.getMaxFractionDigits=function()
{
return this._maxFractionDigits;
}
TrNumberConverter.prototype.setMaxIntegerDigits=function(a19)
{
this._maxIntegerDigits=a19;
}
TrNumberConverter.prototype.getMaxIntegerDigits=function()
{
return this._maxIntegerDigits;
}
TrNumberConverter.prototype.setMinFractionDigits=function(a20)
{
this._minFractionDigits=a20;
}
TrNumberConverter.prototype.getMinFractionDigits=function()
{
return this._minFractionDigits;
}
TrNumberConverter.prototype.setMinIntegerDigits=function(a21)
{
this._minIntegerDigits=a21;
}
TrNumberConverter.prototype.getMinIntegerDigits=function()
{
return this._minIntegerDigits;
}
TrNumberConverter.prototype.setNegativePrefix=function(a22)
{
this._negativePrefix=a22;
}
TrNumberConverter.prototype.getNegativePrefix=function()
{
return this._negativePrefix;
}
TrNumberConverter.prototype.setNegativeSuffix=function(a23)
{
this._negativeSuffix=a23;
}
TrNumberConverter.prototype.getNegativeSuffix=function()
{
return this._negativeSuffix;
}
TrNumberConverter.prototype.isDisabled=function()
{
return this._isDisabled;
}
TrNumberConverter.prototype.setGroupingUsed=function(a24)
{
this._groupingUsed=a24;
}
TrNumberConverter.prototype.isGroupingUsed=function()
{
return this._groupingUsed;
}
TrNumberConverter.prototype.setIntegerOnly=function(a25)
{
this._integerOnly=a25;
}
TrNumberConverter.prototype.isIntegerOnly=function()
{
return this._integerOnly;
}
TrNumberConverter.prototype.setRoundingMode=function(a26)
{
this._roundingMode=a26;
}
TrNumberConverter.prototype.getRoundingMode=function()
{
return this._roundingMode;
}
TrNumberConverter.prototype.getFormatHint=function()
{
if(this._messages&&this._messages["hintPattern"])
{
return TrMessageFactory.createCustomMessage(
this._messages["hintPattern"],
this._pattern);
}
else
{
if(this._pattern)
{
return TrMessageFactory.createMessage(
"org.apache.myfaces.trinidad.convert.NumberConverter.FORMAT_HINT",
this._pattern);
}
else
{
return null;
}
}
}
TrNumberConverter.prototype.getAsString=function(
a27,
a28
)
{
if(this.isDisabled())
return a27;
if(this._isConvertible())
{
if(this._type=="percent"||this._type=="currency")
{
return this._numberFormat.format(a27);
}
else
{
if(typeof a27==="string")
{
return this._numberFormat.format(parseFloat(a27));
}
else
{
var a29=(this._allowClientRounding())?a27.toFixed(this._numberFormat.getMaximumFractionDigits()):a27;
return this._numberFormat.format(parseFloat(a29));
}
}
}
else
{
return undefined;
}
}
TrNumberConverter.prototype.getAsObject=function(
a30,
a31
)
{
if(this.isDisabled())
return a30;
a30=TrFormatUtils.trim(a30);
if(this._isConvertible(a30))
{
if(a30==null)
return null;
if(a30.length==0)
return null
var a32;
var a33=getLocaleSymbols(this._locale);
var a34=false;
var a35=this._numberFormat.hasPrefixOrSuffix(a30);
try
{
if(a35)
{
var a36=this._numberFormat.removePrefixAndSuffix(a30);
a30=a36[0];
a34=a36[1];
}
var a37=a33.getGroupingSeparator();
if(a37=="\xa0")
{
var a38=new RegExp("\\ ","g");
a30=a30.replace(a38,"\xa0");
}
var a39=new RegExp("\\"+a37,"g");
a30=a30.replace(a39,"");
var a40=a33.getDecimalSeparator();
var a41=new RegExp("\\"+a40,"g");
a30=a30.replace(a41,".");
if(a35)
{
a30=this._numberFormat.addPrefixAndSuffix(a30,a34);
}
a30=this._numberFormat.parse(a30)+"";
}
catch(e)
{
try
{
var a42=TrNumberFormat.getNumberInstance();
a42.setMinimumIntegerDigits(this._minIntegerDigits);
a42.setMaximumIntegerDigits(this._maxIntegerDigits);
a42.setMinimumFractionDigits(this._minFractionDigits);
a42.setMaximumFractionDigits(this._maxFractionDigits);
a30=a42.parse(a30)+"";
}
catch(e)
{
var a43;
var a44=this._numberFormat.format(this._example);
var a45="org.apache.myfaces.trinidad.convert.NumberConverter.CONVERT_"+this._type.toUpperCase();
if(this._messages&&this._messages[this._type])
{
a43=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a45),this._messages[this._type],a31,a30,a44);
}
else
{
a43=_createFacesMessage(a45,a31,a30,a44);
}
throw new TrConverterException(a43);
}
}
var a46=new RegExp("\\"+".","g");
a30=a30.replace(a46,getLocaleSymbols().getDecimalSeparator());
a32=_decimalParse(a30,
this._messages,
"org.apache.myfaces.trinidad.convert.NumberConverter",
null,
null,
null,
null,
a31,
!this.isIntegerOnly());
var a47=(this._allowClientRounding())?a32.toFixed(this._numberFormat.getMaximumFractionDigits()):a32;
a32=parseFloat(a47);
if(this._type=="percent")
{
a32=this._numberFormat.moveDecimalLeft(a32);
}
return a32;
}
else
{
return undefined;
}
}
TrNumberConverter.prototype._isConvertible=function(a48)
{
if(this._pattern!=null)
return false;
return TrFormatUtils.isNumberConvertible(a48);
}
TrNumberConverter.prototype._allowClientRounding=function()
{
return this._roundingMode==null||this._roundingMode==TrNumberConverter.ROUND_HALF_UP;
}
TrNumberConverter.prototype._initNumberFormat=function(
a49,
a50,
a51,
a52,
a53)
{
var a54={
"currencyCode":a50,
"currencySymbol":a51,
"negativePrefix":a52,
"negativeSuffix":a53,
"isGroupingUsed":this.isGroupingUsed(),
"maxFractionDigits":this.getMaxFractionDigits(),
"maxIntegerDigits":this.getMaxIntegerDigits(),
"minFractionDigits":this.getMinFractionDigits(),
"minIntegerDigits":this.getMinIntegerDigits(),
"roundingMode":this.getRoundingMode()
};
if(this._type=="percent")
{
this._example=0.3423;
this._numberFormat=TrNumberFormat.getPercentInstance(a49,a54);
}
else if(this._type=="currency")
{
this._example=10250;
this._numberFormat=TrNumberFormat.getCurrencyInstance(a49,a54);
}
else if(this._type=="number")
{
this._numberFormat=TrNumberFormat.getNumberInstance(a49,a54);
}
}

function TrIntegerConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrIntegerConverter";
}
TrIntegerConverter.prototype=new TrConverter();
TrIntegerConverter.prototype.getFormatHint=function()
{
return null;
}
TrIntegerConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrIntegerConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.IntegerConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrLongConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrLongConverter";
}
TrLongConverter.prototype=new TrConverter();
TrLongConverter.prototype.getFormatHint=function()
{
return null;
}
TrLongConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrLongConverter.prototype.getAsObject=function(
a7,
a8
)
{
if(TrFormatUtils.isNumberConvertible(a7))
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.LongConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
else
{
return undefined;
}
}
function TrShortConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrShortConverter";
}
TrShortConverter.prototype=new TrConverter();
TrShortConverter.prototype.getFormatHint=function()
{
return null;
}
TrShortConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrShortConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.ShortConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrByteConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrByteConverter";
}
TrByteConverter.prototype=new TrConverter();
TrByteConverter.prototype.getFormatHint=function()
{
return null;
}
TrByteConverter.prototype.getAsString=function(
a5,
a6
)
{
return""+a5;
}
TrByteConverter.prototype.getAsObject=function(
a7,
a8
)
{
return _decimalParse(a7,
this._message,
"org.apache.myfaces.trinidad.convert.ByteConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a8,
null);
}
function TrDoubleConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrDoubleConverter";
}
TrDoubleConverter.prototype=new TrConverter();
TrDoubleConverter.prototype.getFormatHint=function()
{
return null;
}
TrDoubleConverter.prototype.getAsString=function(
a5,
a6
)
{
var a7=""+a5;
var a8=a7.indexOf(".");
if(a8!=-1)
return a7;
else
return""+a5.toFixed(1);
}
TrDoubleConverter.prototype.getAsObject=function(
a9,
a10
)
{
return _decimalParse(a9,
this._message,
"org.apache.myfaces.trinidad.convert.DoubleConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a10,
true,
true);
}
function TrFloatConverter(
a0,
a1,
a2,
a3,
a4)
{
this._message=a0;
this._maxPrecision=a1;
this._maxScale=a2;
this._maxValue=a3;
this._minValue=a4;
this._class="TrFloatConverter";
}
TrFloatConverter.prototype=new TrConverter();
TrFloatConverter.prototype.getFormatHint=function()
{
return null;
}
TrFloatConverter.prototype.getAsString=function(
a5,
a6
)
{
var a7=""+a5;
var a8=a7.indexOf(".");
if(a8!=-1)
return a7;
else
return""+a5.toFixed(1);
}
TrFloatConverter.prototype.getAsObject=function(
a9,
a10
)
{
return _decimalParse(a9,
this._message,
"org.apache.myfaces.trinidad.convert.FloatConverter",
this._maxPrecision,
this._maxScale,
this._maxValue,
this._minValue,
a10,
true,
true);
}
function TrRangeValidator(
a0,
a1,
a2)
{
this._maxValue=a0;
this._minValue=a1;
this._messages=a2;
this._class="TrRangeValidator";
}
TrRangeValidator.prototype=new TrValidator();
TrRangeValidator.prototype.getHints=function(
a3
)
{
return _returnRangeHints(
this._messages,
this._maxValue,
this._minValue,
"org.apache.myfaces.trinidad.validator.RangeValidator.MAXIMUM_HINT",
"org.apache.myfaces.trinidad.validator.RangeValidator.MINIMUM_HINT",
"org.apache.myfaces.trinidad.validator.RangeValidator.RANGE_HINT",
"hintMax",
"hintMin",
"hintRange"
);
}
TrRangeValidator.prototype.validate=function(
a4,
a5,
a6
)
{
if(a4==null)
return null;
string=""+a4;
numberValue=parseFloat(string);
var a7;
if(this._minValue!=null&&this._maxValue!=null)
{
if(numberValue>=this._minValue&&numberValue<=this._maxValue)
{
return string;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.LongRangeValidator.NOT_IN_RANGE";
if(this._messages&&this._messages["range"])
{
a7=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a8),
this._messages["range"],
a5,
string,
""+this._minValue,
""+this._maxValue);
}
else
{
a7=_createFacesMessage(a8,
a5,
string,
""+this._minValue,
""+this._maxValue);
}
}
}
else
{
if(this._minValue!=null)
{
if(numberValue>=this._minValue)
{
return string;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.LongRangeValidator.MINIMUM";
if(this._messages&&this._messages["min"])
{
a7=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a8),
this._messages["min"],
a5,
string,
""+this._minValue);
}
else
{
a7=_createFacesMessage(a8,
a5,
string,
""+this._minValue);
}
}
}
else
{
if(this._maxValue==null||numberValue<=this._maxValue)
{
return string;
}
else
{
var a8="org.apache.myfaces.trinidad.validator.LongRangeValidator.MAXIMUM";
if(this._messages&&this._messages["max"])
{
a7=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a8),
this._messages["max"],
a5,
string,
""+this._maxValue);
}
else
{
a7=_createFacesMessage(a8,
a5,
string,
""+this._maxValue);
}
}
}
}
throw new TrConverterException(a7);
}
function TrLengthValidator(
a0,
a1,
a2)
{
this._maxValue=a0;
this._minValue=a1;
this._messages=a2;
this._class="TrLengthValidator";
}
TrLengthValidator.prototype=new TrValidator();
TrLengthValidator.prototype.getHints=function(
a3
)
{
var a4=this._minValue;
if(this._maxValue!=null&&this._minValue!=null&&this._minValue==0)
{
a4=null;
}
return _returnRangeHints(
this._messages,
this._maxValue,
a4,
"org.apache.myfaces.trinidad.validator.LengthValidator.MAXIMUM_HINT",
"org.apache.myfaces.trinidad.validator.LengthValidator.MINIMUM_HINT",
(this._minValue==this._maxValue)
?"org.apache.myfaces.trinidad.validator.LengthValidator.EXACT_HINT"
:"org.apache.myfaces.trinidad.validator.LengthValidator.RANGE_HINT",
"hintMax",
"hintMin",
"hintRange"
);
}
TrLengthValidator.prototype.validate=function(
a5,
a6,
a7
)
{
if(a5==null)
return null;
var a8=""+a5;
var a9=a8.length;
if(a9>=this._minValue&&
((this._maxValue==null)||(a9<=this._maxValue)))
{
return a8;
}
else
{
if((this._minValue>0)&&(this._maxValue!=null))
{
var a10=(this._minValue==this._maxValue);
var a11=a10
?"org.apache.myfaces.trinidad.validator.LengthValidator.EXACT"
:"org.apache.myfaces.trinidad.validator.LengthValidator.NOT_IN_RANGE";
var a12;
var a13="range";
if(this._messages&&this._messages[a13])
{
a12=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a11),
this._messages[a13],
a6,
a8,
""+this._minValue,
""+this._maxValue);
}
else
{
a12=_createFacesMessage(a11,
a6,
a8,
""+this._minValue,
""+this._maxValue);
}
throw new TrConverterException(a12);
}
else if(a9<this._minValue)
{
var a11="org.apache.myfaces.trinidad.validator.LengthValidator.MINIMUM";
var a12;
if(this._messages&&this._messages["min"])
{
a12=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a11),
this._messages["min"],
a6,
a8,
""+this._minValue);
}
else
{
a12=_createFacesMessage(a11,
a6,
a8,
""+this._minValue);
}
throw new TrConverterException(a12);
}
else
{
var a11="org.apache.myfaces.trinidad.validator.LengthValidator.MAXIMUM";
var a12;
if(this._messages&&this._messages["max"])
{
a12=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a11),
this._messages["max"],
a6,
a8,
""+this._maxValue);
}
else
{
a12=_createFacesMessage(a11,
a6,
a8,
""+this._maxValue);
}
throw new TrConverterException(a12);
}
}
}
function TrDateTimeRangeValidator(
a0,
a1,
a2,
a3,
a4
)
{
this._maxValue=a0;
this._maxISODate=a3;
this._minValue=a1;
this._minISODate=a4;
this._messages=a2;
this._class="TrDateTimeRangeValidator";
}
TrDateTimeRangeValidator.prototype=new TrValidator();
TrDateTimeRangeValidator.prototype.getHints=function(
a5
)
{
var a6=null;
var a7=null;
if(this._maxValue)
a6=this._maxValue;
if(this._minValue)
a7=this._minValue;
return _returnRangeHints(
this._messages,
a6,
a7,
"org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MAXIMUM_HINT",
"org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MINIMUM_HINT",
"org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.RANGE_HINT",
"hintMax",
"hintMin",
"hintRange"
);
}
TrDateTimeRangeValidator.prototype.validate=function(
a8,
a9,
a10
)
{
if(a8==null)
return null;
dateTime=a8.getTime();
var a11;
var a12=this._getISOConverter();
if(this._minValue&&this._maxValue)
{
try
{
minDate=(this._minISODate==null)?
a10.getAsObject(this._minValue).getTime():
a12.getAsObject(this._minISODate).getTime();
maxDate=(this._maxISODate==null)?
a10.getAsObject(this._maxValue).getTime():
a12.getAsObject(this._maxISODate).getTime();
}
catch(e)
{
return a8;
}
if(dateTime>=minDate&&dateTime<=maxDate)
{
return a8;
}
else
{
var a13="org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.NOT_IN_RANGE";
if(this._messages&&this._messages["range"])
{
a11=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a13),
this._messages["range"],
a9,
""+a10.getAsString(a8),
""+this._minValue,
""+this._maxValue);
}
else
{
a11=_createFacesMessage(a13,
a9,
""+a10.getAsString(a8),
""+this._minValue,
""+this._maxValue);
}
}
}
else
{
if(this._minValue)
{
try
{
minDate=(this._minISODate==null)?
a10.getAsObject(this._minValue).getTime():
a12.getAsObject(this._minISODate).getTime();
}
catch(e)
{
return a8;
}
if(dateTime>=minDate)
{
return a8;
}
else
{
var a13="org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MINIMUM";
if(this._messages&&this._messages["min"])
{
a11=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a13),
this._messages["min"],
a9,
""+a10.getAsString(a8),
""+this._minValue);
}
else
{
a11=_createFacesMessage(a13,
a9,
""+a10.getAsString(a8),
""+this._minValue);
}
}
}
else if(this._maxValue)
{
try
{
maxDate=(this._maxISODate==null)?
a10.getAsObject(this._maxValue).getTime():
a12.getAsObject(this._maxISODate).getTime();
}
catch(e)
{
return a8;
}
if(dateTime<=maxDate)
{
return a8;
}
else
{
var a13="org.apache.myfaces.trinidad.validator.DateTimeRangeValidator.MAXIMUM";
if(this._messages&&this._messages["max"])
{
a11=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a13),
this._messages["max"],
a9,
""+a10.getAsString(a8),
""+this._maxValue);
}
else
{
a11=_createFacesMessage(a13,
a9,
""+a10.getAsString(a8),
""+this._maxValue);
}
}
}
else
{
return a8;
}
}
throw new TrConverterException(a11);
}
TrDateTimeRangeValidator.prototype._getISOConverter=function()
{
if(this._ISO_CONVERTER==null)
this._ISO_CONVERTER=new TrDateTimeConverter("yyyy-MM-dd HH:mm:ss",null,null,null,null);
return this._ISO_CONVERTER;
}
function TrDateRestrictionValidator(
a0,
a1,
a2)
{
this._weekdaysValue=a0;
this._monthValue=a1;
this._messages=a2;
this._weekdaysMap={'2':'tue','4':'thu','6':'sat','1':'mon','3':'wed','5':'fri','0':'sun'};
this._translatedWeekdaysMap={'sun':'0','mon':'1','tue':'2','wed':'3','thu':'4','fri':'5','sat':'6'};
this._monthMap={'2':'mar','4':'may','9':'oct','8':'sep','11':'dec','6':'jul','1':'feb','3':'apr','10':'nov','7':'aug','5':'jun','0':'jan'};
this._translatedMonthMap={'jan':'0','feb':'1','mar':'2','apr':'3','may':'4','jun':'5','jul':'6','aug':'7','sep':'8','oct':'9','nov':'10','dec':'11'};
this._class="TrDateRestrictionValidator";
}
TrDateRestrictionValidator.prototype=new TrValidator();
TrDateRestrictionValidator.prototype.getHints=function(
a3
)
{
return _returnHints(
this._messages,
!this._weekdaysValue?this._weekdaysValue:this._translate(this._weekdaysValue,this._translatedWeekdaysMap,a3.getLocaleSymbols().getWeekdays()),
!this._monthValue?this._monthValue:this._translate(this._monthValue,this._translatedMonthMap,a3.getLocaleSymbols().getMonths()),
"org.apache.myfaces.trinidad.validator.DateRestrictionValidator.WEEKDAY_HINT",
"org.apache.myfaces.trinidad.validator.DateRestrictionValidator.MONTH_HINT",
"hintWeek",
"hintMonth"
);
}
TrDateRestrictionValidator.prototype._translate=function(
values,
map,
valueArray
)
{
if(values)
{
var translatedValues=new Array();
var valuesAsArray=eval(values);
for(i=0;i<valuesAsArray.length;i++)
{
translatedValues.push(valueArray[map[valuesAsArray[i].toLowerCase()]]);
}
return eval(translatedValues);
}
else
{
return values;
}
}
TrDateRestrictionValidator.prototype._removeDisabledValues=function(
a4,
a5
)
{
if(a4&&a5)
{
for(i=0;i<a5.length;i++)
{
if(a4[a5[i].toLowerCase()]!=undefined)
{
a5.splice(i,1);
i--;
}
}
}
}
TrDateRestrictionValidator.prototype.validate=function(
value,
label,
converter
)
{
if(value==null)
return null
submittedDay=value.getDay();
weekDaysArray=eval(this._weekdaysValue);
if(weekDaysArray)
{
var dayString=this._weekdaysMap[submittedDay];
for(var i=0;i<weekDaysArray.length;++i)
{
if(weekDaysArray[i].toLowerCase()==dayString)
{
var days=_trToString(this._translate([dayString],this._translatedWeekdaysMap,converter.getLocaleSymbols().getWeekdays()));
var facesMessage;
var key="org.apache.myfaces.trinidad.validator.DateRestrictionValidator.WEEKDAY";
if(this._messages&&this._messages["days"])
{
facesMessage=_createCustomFacesMessage(TrMessageFactory.getSummaryString(key),
this._messages["days"],
label,
""+converter.getAsString(value),
days);
}
else
{
facesMessage=_createFacesMessage(key,
label,
""+converter.getAsString(value),
days);
}
throw new TrConverterException(facesMessage);
}
}
}
submittedMonth=value.getMonth();
monthArray=eval(this._monthValue);
if(monthArray)
{
var monthString=this._monthMap[submittedMonth];
for(var i=0;i<monthArray.length;++i)
{
if(monthArray[i].toLowerCase()==monthString)
{
var month=_trToString(this._translate([monthString],this._translatedMonthMap,converter.getLocaleSymbols().getMonths()));
var facesMessage;
var key="org.apache.myfaces.trinidad.validator.DateRestrictionValidator.MONTH";
if(this._messages&&this._messages["month"])
{
facesMessage=_createCustomFacesMessage(TrMessageFactory.getSummaryString(key),
this._messages["month"],
label,
""+converter.getAsString(value),
month);
}
else
{
facesMessage=_createFacesMessage(key,
label,
""+converter.getAsString(value),
month);
}
throw new TrConverterException(facesMessage);
}
}
}
return value;
}
function _decimalParse(
a0,
a1,
a2,
a3,
a4,
a5,
a6,
a7,
a8,
a9
)
{
if(a0==null)
return null;
a0=TrFormatUtils.trim(a0);
if(a0.length==0)
return null
var a10=null;
var a11=getLocaleSymbols();
if(a11&&(a9!=true))
{
var a12=a11.getGroupingSeparator();
if((a0.indexOf(a12)==0)||
(a0.lastIndexOf(a12)==(a0.length-1)))
{
a10=_createFacesMessage(a2+".CONVERT",
a7,
a0);
throw new TrConverterException(a10);
}
if(a12=="\xa0"){
var a13=new RegExp("\\ ","g");
a0=a0.replace(a13,"\xa0");
}
var a14=new RegExp("\\"+a12,"g");
a0=a0.replace(a14,"");
var a15=new RegExp("\\"+a11.getDecimalSeparator(),"g");
a0=a0.replace(a15,".");
var a16=document.documentElement["dir"].toUpperCase()=="LTR";
if(!a16)
{
var a17=a0.length;
if(a0.lastIndexOf("-")==(a17-1))
{
a0="-"+a0.substring(0,a17-1);
}
}
}
if((a0.indexOf('e')<0)&&
(a0.indexOf('E')<0)&&
(((a0*a0)==0)||
((a0/a0)==1)))
{
var a18=null;
var a19=false;
if(a8!=null)
{
a0=TrNumberFormat.trimLeadingZeroes(a0);
a18=a8?parseFloat(a0):parseInt(a0);
}
else
{
a18=parseInt(a0);
if(Math.abs(a18)<Math.abs(parseFloat(a0)))
{
a19=true;
}
}
if(!a19&&!isNaN(a18))
{
var a20=a0.length;
var a21=0;
var a22=a0.lastIndexOf('.');
if(a22!=-1)
{
a20=a22;
a21=parseInt(a0.length-parseInt(a22+1));
}
var a23;
var a24;
if((a5!=null)&&
(a18>a5))
{
a23=a2+".MAXIMUM";
a24=a5;
}
else if((a6!=null)&&
(a18<a6))
{
a23=a2+".MINIMUM";
a24=a6;
}
if(a23)
{
a10=_createFacesMessage(a23,
a7,
a0,
""+a24);
throw new TrConverterException(a10);
}
return a18;
}
}
var a25=null;
var a26=false;
if(a2.indexOf("NumberConverter")==-1)
{
a25=a2+".CONVERT";
}
else
{
a25=a2+".CONVERT_NUMBER";
if(a1&&a1["number"])
{
a10=_createCustomFacesMessage(TrMessageFactory.getSummaryString(a25),
a1["number"],
a7,
a0);
a26=true;
}
}
if(!a26)
{
a10=_createFacesMessage(a25,
a7,
a0);
}
throw new TrConverterException(a10);
}
function TrRegExpValidator(
a0,
a1
)
{
this._pattern=a0;
this._messages=a1;
this._class="TrRegExpValidator";
}
TrRegExpValidator.prototype=new TrValidator();
TrRegExpValidator.prototype.getHints=function(
a2
)
{
var a3=null;
if(this._messages["hint"])
{
a3=new Array();
a3.push(TrMessageFactory.createCustomMessage(
this._messages["hint"],
""+this._pattern)
);
}
return a3;
}
TrRegExpValidator.prototype.validate=function(
a4,
a5,
a6
)
{
if(a4==null)
return null;
a4=a4+'';
var a7="^("+this._pattern+")$";
var a8=a4.match(a7);
if((a8!=(void 0))&&(a8[0]==a4))
{
return a4;
}
else
{
var a9="org.apache.myfaces.trinidad.validator.RegExpValidator.NO_MATCH";
var a10;
if(this._messages&&this._messages["detail"])
{
a10=_createCustomFacesMessage(
TrMessageFactory.getSummaryString(a9),
this._messages["detail"],
a5,
a4,
this._pattern);
}
else
{
a10=_createFacesMessage(a9,
a5,
a4,
this._pattern);
}
throw new TrValidatorException(a10);
}
}
function _returnRangeHints(
a0,
a1,
a2,
a3,
a4,
a5,
a6,
a7,
a8
)
{
if(a1!=null&&a2!=null)
{
var a9=new Array();
if(a0&&a0[a8])
{
a9.push(
TrMessageFactory.createCustomMessage(
a0[a8],
""+a2,
""+a1)
);
}
else
{
a9.push(
TrMessageFactory.createMessage(
a5,
""+a2,
""+a1)
);
}
return a9;
}
return _returnHints(
a0,
a1,
a2,
a3,
a4,
a6,
a7
);
}
function _trToString(a0)
{
if(Array.prototype.isPrototypeOf(a0))
{
return a0.join(", ");
}
else
{
return""+a0;
}
}
function _returnHints(
a0,
a1,
a2,
a3,
a4,
a5,
a6
)
{
var a7;
if(a1!=null)
{
a7=new Array();
if(a0&&a0[a5])
{
a7.push(
TrMessageFactory.createCustomMessage(
a0[a5],
_trToString(a1))
);
}
else
{
a7.push(
TrMessageFactory.createMessage(
a3,
_trToString(a1))
);
}
}
if(a2!=null)
{
if(!a7)
{
a7=new Array();
}
if(a0&&a0[a6])
{
a7.push(
TrMessageFactory.createCustomMessage(
a0[a6],
_trToString(a2))
);
}
else
{
a7.push(
TrMessageFactory.createMessage(
a4,
_trToString(a2))
);
}
}
return a7;
}
