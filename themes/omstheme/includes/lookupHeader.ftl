<#-- This file has been written by @author Prasnath.k -->
<!DOCTYPE html>
<html>
<head>
	<link rel="icon" href="/omstheme/images/logo.png" type="image/ico">
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<meta content="IE=edge,chrome=1" http-equiv="X-UA-Compatible">
	<!--<meta content="width=device-width" name="viewport">-->
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
	
	<title><#if title?has_content>${title}<#elseif titleProperty?has_content>${uiLabelMap.get(titleProperty)}</#if></title>
	
	<meta name="description" content="${pageDescriptionLabel!}" />
	<meta name="keywords" content="${keywordsLabel!}" />
	<@icon/>
	<#if layoutSettings.styleSheets?has_content>
    	<#--layoutSettings.styleSheets is a list of style sheets. So, you can have a user-specified "main" style sheet, AND a component style sheet.-->
    	<#list layoutSettings.styleSheets as styleSheet>
      		<link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
    	</#list>
  	</#if>
  	<#if layoutSettings.VT_STYLESHEET?has_content>
    	<#list layoutSettings.VT_STYLESHEET as styleSheet>
      		<link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
    	</#list>
  	</#if>
  	<#if layoutSettings.rtlStyleSheets?has_content && langDir?exists && langDir == "rtl">
    	<#--layoutSettings.rtlStyleSheets is a list of rtl style sheets.-->
    	<#list layoutSettings.rtlStyleSheets as styleSheet>
      		<link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
    	</#list>
  	</#if>
  	<#if layoutSettings.VT_RTL_STYLESHEET?has_content && langDir?exists && langDir == "rtl">
    	<#list layoutSettings.VT_RTL_STYLESHEET as styleSheet>
      		<link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
    	</#list>
  	</#if>

	${layoutSettings.extraHead?if_exists}
  	<#if layoutSettings.VT_EXTRA_HEAD?has_content>
    	<#list layoutSettings.VT_EXTRA_HEAD as extraHead>
      		${extraHead}
    	</#list>
  	</#if>

	<!--<link type="text/css" href="/floral/css/bootstrap-combined.css" 	rel="stylesheet" />-->	

	<!--<link href="http://netdna.bootstrapcdn.com/font-awesome/3.2.1/css/font-awesome.min.css" rel="stylesheet">-->
	
	<!-- BEGIN GLOBAL MANDATORY STYLES -->
	<link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,700&subset=all" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/simple-line-icons.min.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/uniform.default.css" rel="stylesheet" type="text/css"/>
	<!-- END GLOBAL MANDATORY STYLES -->
	
	<!-- BEGIN PAGE LEVEL STYLES -->
	<link href="/omstheme/css/select2.css" rel="stylesheet" type="text/css"/>
	<link rel="stylesheet" type="text/css" href="/omstheme/css/jquery.notific8.min.css"/>
	<link rel="stylesheet" type="text/css" href="/omstheme/css/toastr.min.css"/>
	<link href="/omstheme/css/themes/typeahead.css" rel="stylesheet" type="text/css"/>
	<!-- END PAGE LEVEL SCRIPTS -->
	
	<!-- BEGIN PAGE STYLES -->
	<!--<link href="/fd_css/tasks.css" rel="stylesheet" type="text/css"/>-->
	<link rel="stylesheet" type="text/css" href="/omstheme/css/fancybox/jquery.fancybox.css"/>
	<!-- END PAGE STYLES -->
	
	<!-- BEGIN THEME STYLES -->
	<#--<link href="/omstheme/css/components-md.css" id="style_components" rel="stylesheet" type="text/css"/>-->
	<link href="/omstheme/css/themes/darkblue.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/plugins-md.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/custom.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/bootstrap-switch.min.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/themes/typeahead.css" rel="stylesheet" type="text/css"/>
	<link href="/omstheme/css/pages/pricing.min.css" rel="stylesheet" type="text/css"/>
	<!-- END THEME STYLES -->
	
	<#--<script language="JavaScript" type="text/javascript" src="/images/prototypejs/prototype.js"></script>-->
	
	<#--layoutSettings.javaScripts is a list of java scripts. -->
    <#-- use a Set to make sure each javascript is declared only once, but iterate the list to maintain the correct order -->
	<#if layoutSettings.javaScripts?has_content>
    	<#assign javaScriptsSet = Static["org.ofbiz.base.util.UtilMisc"].toSet(layoutSettings.javaScripts)/>
    	<#list layoutSettings.javaScripts as javaScript>
      		<#if javaScriptsSet.contains(javaScript)>
        		<#assign nothing = javaScriptsSet.remove(javaScript)/>
        		<script type="text/javascript" src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>"></script>
      		</#if>
    	</#list>
  	</#if>
	<#if layoutSettings.VT_HDR_JAVASCRIPT?has_content>
    	<#list layoutSettings.VT_HDR_JAVASCRIPT as javaScript>
      		<script type="text/javascript" src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>"></script>
    	</#list>
  	</#if>
  	
  	<!-- BEGIN JAVASCRIPTS(Load javascripts at bottom, this will reduce page load time) -->
	<!-- BEGIN CORE PLUGINS -->
	<!--[if lt IE 9]>
	<script src="../../assets/global/plugins/respond.min.js"></script>
	<script src="../../assets/global/plugins/excanvas.min.js"></script> 
	<![endif]-->
	<script src="/opentaps_js/fieldlookup.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery-ui.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery.validate.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery-migrate.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/bootstrap/bootstrap.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery.blockui.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery.uniform.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/jquery.cokie.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/bootstrap/validator.js" type="text/javascript"></script>
	<script src="/omstheme/js/handlebars.min.js"></script>
	<script src="/omstheme/js/typeahead.bundle.min.js"></script>
	<!-- END CORE PLUGINS -->
	
	<!-- BEGIN PAGE LEVEL PLUGINS -->
	
	<script src="/omstheme/js/jquery/jquery.backstretch.min.js" type="text/javascript"></script>
	<script type="text/javascript" src="/omstheme/js/select2/select2.min.js"></script>
	
	<script src="/omstheme/js/amcharts/amcharts.js" type="text/javascript"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.mixitup.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.fancybox.pack.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.notific8.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.pulsate.min.js"></script>
	<script src="/omstheme/js/bootstrap/bootstrap-switch.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/bootstrap/jquery.bootstrap.wizard.min.js" type="text/javascript"></script>
	<script type="text/javascript" src="/omstheme/js/bootstrap/bootstrap-select.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/select2/select2.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.dataTables.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/dataTables.tableTools.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/dataTables.colReorder.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/dataTables.scroller.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/bootstrap/dataTables.bootstrap.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.multi-select.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.notific8.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/bootstrap/bootbox.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/toastr.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.pulsate.min.js"></script>
	
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.mixitup.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/jquery/jquery.fancybox.pack.js"></script>
	<script type="text/javascript" src="/omstheme/js/pace.min.js"></script>
	<script type="text/javascript" src="/omstheme/js/fnReloadAjax.js"></script>
	<!-- END PAGE LEVEL PLUGINS -->
	
	<!-- BEGIN PAGE LEVEL SCRIPTS -->
	<script src="/omstheme/js/metronic/metronic.js" type="text/javascript"></script>
	<script src="/omstheme/js/layout.js" type="text/javascript"></script>
	<script src="/omstheme/js/demo.js" type="text/javascript"></script>
	<!-- END PAGE LEVEL SCRIPTS -->
	
	<script src="/omstheme/js/custom.js" type="text/javascript"></script>
	<script src="/omstheme/js/rating.js" type="text/javascript"></script>
	
	
	<link href="/omstheme/css/categoryCssPlugin/aciTree.css" rel="stylesheet" type="text/css" media="all"/>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciPlugin.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciSortable.min.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.dom.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.core.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.utils.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.selectable.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.sortable.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.checkbox.js" type="text/javascript"></script>
	<script src="/omstheme/js/jquery/categoryTreePlugin/jquery.aciTree.radio.js" type="text/javascript"></script>
	
	<meta property="og:description" content="${pageDescriptionLabel!}">
	<meta property="og:title" content="<#if title?has_content>${title}<#elseif titleProperty?has_content>${uiLabelMap.get(titleProperty)}</#if>: ">
	<meta property="og:site_name" content="">
	<meta property="og:type" content="website">
	<#if og_image??>
		<meta property="og:image" content="<@fullUrlPath url=og_image />">
	</#if>

	<script type="text/javascript">
		// This code set the timeout default value for opentaps.sendRequest
		var ajaxDefaultTimeOut = ${configProperties.get("opentaps.ajax.defaultTimeout")};
	</script>        
	<#if gwtScripts?exists>
		<meta name="gwt:property" content="locale=${locale}"/>
	</#if>

	<#--<link rel="apple-touch-icon-precomposed" sizes="114x114" href="/ttt_portal_images/apple-touch-icon.png" />
	<link rel="apple-touch-icon-precomposed" sizes="144x144" href="/ttt_portal_images/apple-touch-icon-144x144.png" />-->
	
	<#--><@includeAnalytics/>-->
	
	
	<#--Apps Style purpose-->	
	<link href="/omstheme/css/dataTables.bootstrap.css" rel="stylesheet" type="text/css"/>
	<link href="/moca_css/custom/style.css" rel="stylesheet" type="text/css"/>
	<link href="/moca_css/custom/uiCustom.css" rel="stylesheet" type="text/css"/>
</head>

<script type="text/javascript">
        // This code set the timeout default value for opentaps.sendRequest
        var ajaxDefaultTimeOut = 10000;
    </script>
    <script type="text/javascript">
        // This code inserts the value lookedup by a popup window back into the associated form element
        var re_id = new RegExp('id=(\\d+)');
        var num_id = (re_id.exec(String(window.location))
                ? new Number(RegExp.$1) : 0);
        var obj_caller = null;
        
        if(num_id != 0 ){
        	obj_caller = (window.opener ? window.opener.lookups[num_id] : null);
        }	
        if (obj_caller == null) 
            obj_caller = window.opener;
        
        
        // function passing selected value to calling window
        function set_value(value) {
        		obj_caller.getMerchantSku(value);
        
                if (!obj_caller) return;
                
                var loc = window.location.href;
                window.close();
                obj_caller.target.value = value;
								
                if (obj_caller.targetHidden == null) return;
								
                obj_caller.targetHidden.value = value;
								
                obj_caller.target.focus();
                if (obj_caller.onLookupReturn) {
                    obj_caller.onLookupReturn();
                }
        }
        
        //Popup mktProduct lookup added by sabari
        function set_value1(value) {
                
                var loc = window.location.href;
                window.close();
                obj_caller.target.value = value;
								
                if (obj_caller.targetHidden == null) return;
								
                obj_caller.targetHidden.value = value;
								
                obj_caller.target.focus();
                if (obj_caller.onLookupReturn) {
                    obj_caller.onLookupReturn();
                }
        }
		
        function set_value_new (value) {
                if (!obj_caller) return;
                window.close();
                obj_caller.target.value = value;
                obj_caller.target.focus();
        }		
		
        // function passing selected value to calling window
        function set_values(value, value2) {
                set_value(value);
                if (!obj_caller.target2) return;
                if (obj_caller.target2 == null) return;
                obj_caller.target2.value = value2;
        }
        function set_multivalues(value) {
            obj_caller.target.value = value;
            var thisForm = obj_caller.target.form;
            var evalString = "";
             
    		if (arguments.length > 2 ) {
        		for(var i=1; i < arguments.length; i=i+2) {
        			evalString = "thisForm." + arguments[i] + ".value='" + arguments[i+1] + "'";
        			eval(evalString);
        		}
    		}
    		window.close();
         }
         // function add value to the end of field content using ';' as separator.
         function add_value(/*String*/ value) {
            if (!obj_caller) return;
            window.close();
            obj_caller.target.value += value;
            obj_caller.target.value += '; ';
         }
         function add_list_value(/*String*/ value) {
            if (!obj_caller) return;
            window.close();
            obj_caller.target.value = value;
            obj_caller.document.assignSmartListForm.submit();
         }
    </script>
</head>
<body>

<div class="page-container">
<!-- BEGIN CONTAINER -->
	<div class="page-content-wrapper">
		<!-- BEGIN CONTENT -->
		<div class="page-content" style="margin-top: 5%">