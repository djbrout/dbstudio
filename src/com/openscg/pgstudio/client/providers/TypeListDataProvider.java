/*
 * PostgreSQL Studio
 */
package com.openscg.pgstudio.client.providers;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.openscg.pgstudio.client.PgStudio;
import com.openscg.pgstudio.client.PgStudio.ITEM_TYPE;
import com.openscg.pgstudio.client.PgStudioService;
import com.openscg.pgstudio.client.PgStudioServiceAsync;
import com.openscg.pgstudio.client.messages.TypesJsObject;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.TypeInfo;
import com.openscg.pgstudio.client.models.TypeInfo.TYPE_KIND;

	public class TypeListDataProvider extends AsyncDataProvider<TypeInfo> implements ModelListProvider
	{
			private List<TypeInfo> typeList = new ArrayList<TypeInfo>();

			private DatabaseObjectInfo schema = null;
			
			private final PgStudioServiceAsync studioService = GWT.create(PgStudioService.class);

			public void setSchema(DatabaseObjectInfo schema) {
				this.schema = schema;
				getData();
			}
			
			public List<TypeInfo> getList()	{
				return typeList;
			}

			public void refresh() {
				getData();				
			}

			
			@Override
			protected void onRangeChanged(HasData<TypeInfo> display)
			{
				getData();
				
				int start = display.getVisibleRange().getStart();
		        int end = start + display.getVisibleRange().getLength();
		        end = end >= typeList.size() ? typeList.size() : end;
		        List<TypeInfo> sub = typeList.subList(start, end);
		        updateRowData(start, sub);
		        
			}

	private void getData() {
		if (schema != null) {
			studioService.getList(PgStudio.getToken(), schema.getId(),
					ITEM_TYPE.TYPE, new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							typeList.clear();
							// Show the RPC error message to the user
							Window.alert(caught.getMessage());
						}

						public void onSuccess(String result) {
							typeList = new ArrayList<TypeInfo>();

							JsArray<TypesJsObject> types = json2Messages(result);

							if (types != null) {
								typeList.clear();

								for (int i = 0; i < types.length(); i++) {
									TypesJsObject type = types.get(i);
									typeList.add(msgToTypeInfo(type));
								}
							}

							updateRowCount(typeList.size(), true);
							updateRowData(0, typeList);

						}
					});
		}
	}

			private TypeInfo msgToTypeInfo(TypesJsObject msg) {
				int id = Integer.parseInt(msg.getId());

				TypeInfo type = new TypeInfo(schema.getId(), id, msg.getName());

				if (msg.getTypeKind().equalsIgnoreCase("c")) {
					type.setKind(TYPE_KIND.COMPOSITE);
				} else if (msg.getTypeKind().equalsIgnoreCase("d")) {
					type.setKind(TYPE_KIND.DOMAIN);					
				} else if (msg.getTypeKind().equalsIgnoreCase("e")) {
					type.setKind(TYPE_KIND.ENUM);					
				} else if (msg.getTypeKind().equalsIgnoreCase("r")) {
					type.setKind(TYPE_KIND.RANGE);					
				} else {
					type.setKind(TYPE_KIND.UNKNOWN);										
				}
				
				return type;
			}

			private static final native JsArray<TypesJsObject> json2Messages(
					String json)
			/*-{ 
			  	return eval(json); 
			}-*/;

	}