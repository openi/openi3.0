<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- if this is a nested component, the global stylesheet parameters $renderId 
		and $border are not used. Instead, the NodeHandler may define @border and 
		@renderId attributes -->
	<xsl:output method="html" indent="no" encoding="ISO-8859-1" />
	<xsl:param name="context" />
	<xsl:param name="renderId" />
	<xsl:param name="token" />
	<xsl:param name="tree-border" select="'1'" />

	<xsl:include href="../../wcf/controls.xsl" />
	<xsl:include href="../../wcf/changeorder.xsl" />
	<xsl:include href="../../wcf/identity.xsl" />

	<xsl:template name="xtree-renderId">
		<xsl:choose>
			<xsl:when test="@renderId">
				<xsl:value-of select="@renderId" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$renderId" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="xtree-border">
		<xsl:choose>
			<xsl:when test="@border">
				<xsl:value-of select="@border" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$tree-border" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- for handwritten xtree element where the tree is part of a form -->

	<xsl:template match="xtree">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="xtree-component">
		<div id="mem-tree-container" style="overflow: auto; background: none repeat scroll 0 0 #F0F0F0;border: 1px solid #CCCCCC;">
			<table cellspacing="0" cellpadding="2" border="0" style="width: 100%; padding: 10px;">
				<xsl:attribute name="id">
			      <xsl:call-template name="xtree-renderId" />
			    </xsl:attribute>
	    		<!-- 
					<xsl:attribute name="border">
				      <xsl:call-template name="xtree-border" />
				    </xsl:attribute>
					<xsl:attribute name="cellpadding">
				      <xsl:call-template name="xtree-border" />
				    </xsl:attribute>
		
					<xsl:if test="@width">
						<xsl:attribute name="width">
					        <xsl:value-of select="@width" />
					      </xsl:attribute>
					</xsl:if>
	 			-->
				<xsl:call-template name="xtree-title" />
				<xsl:apply-templates select="tree-extras-top" />
				<xsl:apply-templates select="tree-node" />
				<xsl:apply-templates select="tree-extras-bottom" />
			</table>
		</div>
		<xsl:apply-templates select="buttons" />
	</xsl:template>


	<xsl:template name="xtree-title">
		<xsl:if test="@title or @closeId">
			<tr>
				<th class="xform-title">
					<table border="0" cellspacing="0" cellpadding="0" width="100%">
						<tr>
							<xsl:if test="@title">
								<th align="left" class="xform-title">
									<xsl:value-of select="@title" />
								</th>
							</xsl:if>
							<!-- 
							<xsl:if test="@closeId">
								<td align="right" class="xform-close-button">
									<input type="image" src="wcf/form/cancel.png" name="{@closeId}"
										width="16" height="16" />
								</td>
							</xsl:if>
							 -->
						</tr>
					</table>
				</th>
			</tr>
		</xsl:if>
		<xsl:if test="@error">
			<tr>
				<td class="xform-error">
					<xsl:value-of select="@error" />
				</td>
			</tr>
		</xsl:if>
	</xsl:template>


	<xsl:template match="tree-node">
		<tr>
			<td nowrap="nowrap" class="tree-node-{@style}">

				<div style="margin-left: {@level}em">
					<!-- checkbox / radiobox is handled by controls.xsl -->
					<xsl:apply-templates select="checkBox|radioButton" />

					<xsl:if test="@buttonId">
						<xsl:choose>
							<xsl:when test="@selected">
								<input border="0" type="checkbox" name="{@buttonId}" id="{@buttonId}"
									onclick="OlapActions.selectDeselectMember(this, pivotID);"
									checked="" />
							</xsl:when>
							<xsl:otherwise>
								<input border="0" type="checkbox" name="{@buttonId}" id="{@buttonId}" 
									onclick="OlapActions.selectDeselectMember(this, pivotID);"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:if>

					<!-- expand/collapse button -->
					<xsl:choose>
						<xsl:when test="@state='bounded'">
							<input border="0" type="image" name="{@id}.unbound" src="wcf/tree/unbound.png"
								width="9" height="9" />
						</xsl:when>
						<xsl:when test="@state='expanded'">
							<input border="0" type="image" name="{@id}.collapse" src="jpivot/collapse-icon.png"
								onclick="OlapActions.collapseMemberTree(this, pivotID); return false;" />
						</xsl:when>
						<xsl:when test="@state='collapsed'">
							<input border="0" type="image" name="{@id}.expand" src="jpivot/expand-icon.png"
								onclick="OlapActions.expandMemberTree(this, pivotID); return false;" />
						</xsl:when>
						<xsl:otherwise>
							<!-- <img src="wcf/tree/leaf.png" width="9" height="9" /> -->
						</xsl:otherwise>
					</xsl:choose>

					<!-- <xsl:apply-templates select="move-button"/> -->
					<xsl:text> </xsl:text>
					<xsl:choose>
						<xsl:when test="@hrefId">
							<!-- <a href="?{$token}&amp;{@hrefId}=x"> -->
							<xsl:value-of select="@label" />
							<!-- </a> -->
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="@label" />
						</xsl:otherwise>
					</xsl:choose>

					<xsl:apply-templates select="delete-button" />

				</div>
			</td>
		</tr>
		<xsl:apply-templates select="tree-node" />
	</xsl:template>

	<xsl:template match="delete-button">
		<xsl:text> </xsl:text>
		<input type="image" border="0" name="{@id}" src="wcf/tree/delete.png"
			width="9" height="9" />
	</xsl:template>

	<xsl:template match="buttons">
		<tr>
			<td align="right">
				<div align="right" style="float: right; margin: 5px">
					<xsl:apply-templates select="button" />
				</div>
			</td>
		</tr>
	</xsl:template>
	
	<xsl:template match="button">
		<xsl:choose>
			<xsl:when test="@label='Ok'">
				<input type="button" name="{@id}" id="{@id}" value="Ok"
					onclick="OlapActions.applyMemberSelection(this, pivotID); return false;" class="openi-btn" />
			</xsl:when>
			<xsl:when test="@label='Cancel'">
				<input type="button" name="{@id}" id="{@id}" value="Cancel" onclick="OlapActions.revertMemberSelection(this, pivotID); return false;" class="openi-btn" />
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="radioButton">
	  <input type="radio" name="{@group-id}" value="{@id}" onclick="OlapActions.selectDeselectMember(this, pivotID); return false;">
	    <xsl:call-template name="stdattrs"/>
	    <xsl:if test="@selected='true'">
	      <xsl:attribute name="checked">checked</xsl:attribute>
	    </xsl:if>
	    <xsl:if test="@disabled='true'">
	      <xsl:attribute name="disabled">disabled</xsl:attribute>
	    </xsl:if>
	  </input>
	  <input type="hidden" name="{@id}.valid" value="x"/>
	  <xsl:apply-templates/>
	</xsl:template>

</xsl:stylesheet>
