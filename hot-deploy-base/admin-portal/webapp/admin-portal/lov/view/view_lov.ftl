<#include "component://bootstrap/lib/ofbizFormMacros.ftl"/>

<div class="row">
	<div id="main" role="main" class="pd-btm-title-bar">
		<div class="col-lg-12 col-md-12 col-sm-12 dash-panel">
		<#assign extra='<a href="/admin-portal/control/findLov" class="btn btn-xs btn-primary">
			<i class="fa fa-chevron-circle-left" aria-hidden="true"></i> Back</a>' />
		<@sectionFrameHeaderTab title="View Lov" tabId="ViewLov" extra=extra/>
		<#include "component://admin-portal/webapp/admin-portal/lov/tab_menu.ftl"/>
		<@navTab
			instanceId="VIEW_LOV"
			activeTabId="details"
			/>
		</div>
	</div>
</div>
