AdfUIComponents.createComponentClass("AdfUIIterator",{componentType:"org.apache.myfaces.trinidad.Iterator",propertyKeys:["value",{name:"rows",type:"Number","default":25},{name:"first",type:"Number","default":0}],superclass:AdfUICollection});
function AdfStampedDragSource(a,b,d){arguments.length&&this.Init(a,b,d)}AdfObject.createSubclass(AdfStampedDragSource,AdfDragSource);AdfStampedDragSource.prototype.Init=function(a,b,d){AdfStampedDragSource.superclass.Init.call(this,a,b);this._modelName=d};AdfStampedDragSource.prototype.getModelName=function(){return this._modelName};
AdfStampedDragSource.prototype.isAvailable=function(a,b){AdfAssert.assertPrototype(a,AdfDnDContext);AdfAssert.assertPrototype(b,AdfUIInputEvent);AdfAssert.DEBUG&&AdfDnDContext.assertUserAction(a.getUserAction());return this.AreDraggedRowKeysAvailable(b)?!0:null!=AdfStampedDragSource.superclass.GetDragTransferable.call(this,b)};AdfStampedDragSource.prototype.getRowKeyDataFlavor=function(){if(this.getComponent()){var a=this._modelName;if(a)return AdfDataFlavor.getRowKeyDataFlavor(a)}return null};
AdfStampedDragSource.prototype.GetDragTransferable=function(a){var b=AdfStampedDragSource.superclass.GetDragTransferable.call(this,a);this.getComponent();var d=this._modelName;if(d&&d.length&&(a=this._getRowKeys(a),AdfAssert.assertArrayOrNull(a),a)){var e=[],f=[];if(null!=b)for(var g=b.getTransferDataFlavors(),h=g.length,k=0;k<h;k++){var l=g[k];e.push(l);f.push(b.getTransferData(l))}b=AdfDataFlavor.getRowKeyDataFlavor(d);f.push(a);e.push(b);return new AdfObjectTransferable(f,e)}return b};
AdfStampedDragSource.prototype.GetDragOffset=function(a){var b=this._getRowKeys(a);return b?this.GetDragOffsetForRowKeys(a,b):{x:a.getOffsetX(),y:a.getOffsetY()}};AdfStampedDragSource.prototype.GetDragOffsetForRowKeys=function(a,b){AdfAssert.assertArray(b);return this.getComponent().getPeer().getDragOffsetForRowKeys(a,b)};
AdfStampedDragSource.prototype.GetDragOverFeedback=function(a){AdfAssert.assertPrototype(a,AdfUIInputEvent);var b=this._getRowKeys(a);return b?this.GetDragOverFeedbackForRowKeys(b):AdfStampedDragSource.superclass.GetDragOverFeedback.call(this,a)};AdfStampedDragSource.prototype.applyDragReadyState=function(a){AdfAssert.assertPrototype(a,AdfUIInputEvent);var b=this._getRowKeys(a);return b?this.ApplyDragReadyStateForRowKeys(a,b):AdfStampedDragSource.superclass.applyDragReadyState.call(this,a)};
AdfStampedDragSource.prototype.ApplyDragReadyStateForRowKeys=function(a,b){AdfAssert.assertArray(b);var d=this.getComponent(),e=d.getPeer();return e.applyDragReadyStateForRowKeys?e.applyDragReadyStateForRowKeys(d,a,b):AdfStampedDragSource.superclass.applyDragReadyState.call(this,a)};
AdfStampedDragSource.prototype.applyDragSourceState=function(a){AdfAssert.assertPrototype(a,AdfDomUIInputEvent);var b=this._getRowKeys(a);return b?this.ApplyDragSourceStateForRowKeys(a,b):AdfStampedDragSource.superclass.applyDragSourceState.call(this,a)};
AdfStampedDragSource.prototype.ApplyDragSourceStateForRowKeys=function(a,b){AdfAssert.assertArray(b);var d=this.getComponent(),e=d.getPeer();return e.applyDragSourceStateForRowKeys?e.applyDragSourceStateForRowKeys(d,a,b):AdfStampedDragSource.superclass.applyDragSourceState.call(this,a)};AdfStampedDragSource.prototype.GetDragOverFeedbackForRowKeys=function(a){AdfAssert.assertArray(a);return this.getComponent().getPeer().getDragNodeForRowKeys(a)};
AdfStampedDragSource.prototype.GetDraggedRowKeys=function(a){AdfAssert.assert(a);return[a]};AdfStampedDragSource.prototype.AreDraggedRowKeysAvailable=function(a){return null!=this._getRowKeys(a)};AdfStampedDragSource.prototype._getRowKeys=function(a){return(a=this.getComponent().getPeer().getRowKeyForEvent(a))?this.GetDraggedRowKeys(a):null};
function AdfTableDragSource(a,b,d){arguments.length&&this.Init(a,b,d)}AdfObject.createSubclass(AdfTableDragSource,AdfStampedDragSource);AdfTableDragSource.prototype.GetDraggedRowKeys=function(a){AdfAssert.assert(a);var b=this.getComponent().getSelectedRowKeys();if(null!=b&&null==b.afrSelectAll&&null!=b[a]){a=[];for(var d in b)a.push(d);return a}return[a]};AdfTableDragSource.prototype.AreDraggedRowKeysAvailable=function(a){return null!=this.getComponent().getPeer().getRowKeyForEvent(a)};
AdfTableDragSource.prototype.isAvailable=function(a,b){AdfAssert.assertPrototype(a,AdfDnDContext);AdfAssert.assertPrototype(b,AdfUIInputEvent);var d=b.getNativeEventTarget();return!this.getComponent().getPeer().isDragAvailable(d)?!1:AdfTableDragSource.superclass.isAvailable.call(this,a,b)};
AdfUIComponents.createComponentClass("AdfUITable",{componentType:"org.apache.myfaces.trinidad.Table",propertyKeys:[{name:"rowDisclosureListener",type:"Object",secured:!0},"disclosedRowKeys",{name:"selectionListener",type:"Object",secured:!0},"selectedRowKeys",{name:"immediate",type:"Boolean","default":!1,secured:!0},{name:"sortListener",type:"Object",secured:!0},{name:"rangeChangeListener",type:"Object",secured:!0},{name:"showAll",type:"Boolean","default":!1,secured:!0}],eventNames:["rowDisclosure",
"selection","rangeChange","sort"],superclass:AdfUIIterator});
AdfUITable.SELECTION_LISTENER_KEY="afrSelListener";AdfUITable.prototype.findComponent=function(a,b){return void 0!=b?(a=this.getClientId()+":"+b+":"+a,AdfPage.PAGE.findComponent(a)):AdfUITable.superclass.findComponent.call(this,a)};AdfUITable.prototype.Init=function(a,b,c,d,e){AdfUITable.superclass.Init.call(this,a,b,c,d,e)};
AdfUITable.prototype.GetChanges=function(){var a=AdfUITable.superclass.GetChanges.call(this),b=a[AdfUITable.DISCLOSED_ROW_KEYS];if(b){var c=[],d=0,e;for(e in b)c[d++]=e;a[AdfUITable.DISCLOSED_ROW_KEYS]=c.join("$afr$")}if(b=a[AdfUITable.SELECTED_ROW_KEYS]){var c=[],f;for(f in b)d=b[f]-1,c[d]=f;a[AdfUITable.SELECTED_ROW_KEYS]=c.join("$afr$")}return a};
AdfUITable.prototype.DeliverDerivedPropertyEvents=function(a,b,c){if(a==AdfUITable.SELECTED_ROW_KEYS&&(a=this.getPeer())&&a.canDeliverSelectionEvent&&a.canDeliverSelectionEvent())(b=AdfRowKeySetChangeEvent.createRowKeySetChangeEvent(this,AdfSelectionEvent.SELECTION_EVENT_TYPE,b,c))&&b.queue()};
AdfUITable.prototype.setDisclosedRowKey=function(a,b){AdfAssert.assertString(a);AdfAssert.assertBoolean(b);if(b==this.isDisclosed(a))return!1;var c=this.getPeer();if(c&&c.isDisclosureFetchPending())return!1;var d=this.getDisclosedRowKeys(),c={};AdfCollections.copyInto(c,d);b?c[a]=!0:delete c[a];(d=AdfRowKeySetChangeEvent.createRowKeySetChangeEvent(this,AdfRowDisclosureEvent.ROW_DISCLOSURE_EVENT_TYPE,d,c))&&d.queue();if(!d||d.isCanceled())return!1;this.setDisclosedRowKeys(c);return!0};
AdfUITable.prototype.isDisclosed=function(a){var b=this.getDisclosedRowKeys();return b?b[a]:!1};AdfUITable.prototype.getSelectedColumns=function(){var a=[],b=this.getPeer();if(b)for(var b=b.__getSelectedColumns(),c=0;c<b.length;c++)a.push(b[c].getClientId());return a};
AdfUIComponents.createComponentClass("AdfUITable2",{componentType:"oracle.adf.Table",propertyKeys:[{name:"rangeChangeListener",type:"Object",secured:!0},{name:"filterModel",type:"Object",secured:!0},{name:"queryListener",type:"Object",secured:!0}],eventNames:["query"],superclass:AdfUITable});
function AdfTableUtils(){this.Init()}AdfObject.createSubclass(AdfTableUtils);AdfTableUtils.InitClass=function(){};AdfTableUtils.prototype.Init=function(){AdfTableUtils.superclass.Init.call()};AdfTableUtils.queueColumnSelectionEvent=function(a,b,d){b=AdfTableUtils.processColumnSelectionEventChangedSets(b,d);d=[];var e=[],f=0,g;for(g in b[0])d[f++]=g;f=0;for(g in b[1])e[f++]=g;AdfColumnSelectionEvent.queue(a,d,e)};
AdfTableUtils.processColumnSelectionEventChangedSets=function(a,b){var d=null,e=null;if(b)if(a)for(var f in b)a[f]||(d||(d={}),d[f]=!0);else d={},AdfCollections.copyInto(d,b);if(a)if(b)for(f in a)b[f]||(e||(e={}),e[f]=!0);else e={},AdfCollections.copyInto(e,a);return[null==e?{}:e,null==d?{}:d]};
AdfTableUtils.queueSortEvent=function(a,b,d){a.setProperty("scrollTopRowKey",null,!0,AdfUIComponent.PROPAGATE_ALWAYS);var e=a.getPeer();e&&(e=e.GetAssociatedComponent(),null!=e&&e.setProperty("scrollTopRowKey",null,!0,AdfUIComponent.PROPAGATE_ALWAYS));(new AdfSortEvent(a,b,d)).queue()};
AdfUIComponents.createComponentClass("AdfUIColumn",{componentType:"org.apache.myfaces.trinidad.Column",propertyKeys:[{name:"sortProperty",type:"String",secured:!0},{name:"sortStrength",type:"String","default":"Identical",secured:!0}]});
AdfUIComponents.createComponentClass("AdfRichColumn",{componentType:"oracle.adf.RichColumn",propertyKeys:[{name:"inlineStyle",type:"String"},{name:"styleClass",type:"String"},{name:"shortDesc",type:"String"},{name:"persist",type:"Array"},{name:"dontPersist",type:"Array"},{name:"align",type:"String"},{name:"headerClass",type:"String"},{name:"footerClass",type:"String"},{name:"width",type:"String","default":"100"},{name:"minimumWidth",type:"String","default":"12"},{name:"headerText",type:"String"},
{name:"noWrap",type:"Boolean","default":!0},{name:"headerNoWrap",type:"Boolean","default":!1},{name:"sortable",type:"Boolean","default":!1},{name:"filterable",type:"Boolean","default":!1},{name:"separateRows",type:"Boolean","default":!1},{name:"rowHeader",type:"String","default":"false"},{name:"selected",type:"Boolean","default":!1},{name:"displayIndex",type:"Number","default":-1},{name:"frozen",type:"Boolean","default":!1},{name:"helpTopicId",type:"String"},{name:"showRequired",type:"Boolean","default":!1},
{name:"visible",type:"Boolean","default":!0},{name:"colSpan",type:"String","default":"1"}],superclass:AdfUIColumn});
AdfRichUIPeer.createPeerClass(AdfRichUIPeer,"AdfDhtmlColumnPeer",!1);AdfDhtmlColumnPeer.InitSubclass=function(){AdfRichUIPeer.addSuppressedPPRAttributes(this,AdfRichColumn.WIDTH,AdfRichColumn.MINIMUM_WIDTH,AdfRichColumn.SELECTED);AdfRichUIPeer.addComponentPropertyChanges(this,AdfRichColumn.WIDTH,AdfRichColumn.SHOW_REQUIRED);AdfRichUIPeer.addComponentPropertyGetters(this,AdfRichColumn.DISPLAY_INDEX);AdfObject.ensureClassInitialization(AdfDhtmlTableBasePeer)};
AdfDhtmlColumnPeer.prototype.Init=function(a,b){AdfDhtmlColumnPeer.superclass.Init.call(this,a,b);"undefined"!=typeof window.AdfColumnDragSource&&(a.setDragSource(new AdfColumnDragSource),a.setDropTarget(new AdfColumnDropTarget))};AdfDhtmlColumnPeer.prototype.GetComponentDisplayIndex=function(a,b){return AdfAgent.AGENT.getIntAttribute(b,"_d_index",-1)};
AdfDhtmlColumnPeer.prototype.ComponentWidthChanged=function(a,b,d,e){for(b=a.getParent();b&&!(b instanceof AdfUICollection);)b=b.getParent();null!=b&&b.getPeer().resizeColumn(a,d,e)};AdfDhtmlColumnPeer.prototype.ScrollIntoView=function(a,b,d){for(a=a.getParent();a&&!(a instanceof AdfUICollection);)a=a.getParent();null!=a&&a.getPeer().scrollColumnIntoView(null,this.getDomElement());null!=d&&AdfLogger.LOGGER.warning("subTargetId not supported for column ScrollIntoView");b&&AdfLogger.LOGGER.warning("Focus not supported for column ScrollIntoView")};
AdfDhtmlColumnPeer.prototype.ComponentShowRequiredChanged=AdfDomUtils.componentShowRequiredChanged;