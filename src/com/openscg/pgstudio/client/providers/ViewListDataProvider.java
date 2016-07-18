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
import com.openscg.pgstudio.client.messages.ViewsJsObject;
import com.openscg.pgstudio.client.models.DatabaseObjectInfo;
import com.openscg.pgstudio.client.models.ViewInfo;

public class ViewListDataProvider extends AsyncDataProvider<ViewInfo> implements
		ModelListProvider {
	private List<ViewInfo> viewList = new ArrayList<ViewInfo>();

	private DatabaseObjectInfo schema = null;

	private final PgStudioServiceAsync studioService = GWT
			.create(PgStudioService.class);

	public void setSchema(DatabaseObjectInfo schema) {
		this.schema = schema;
		getData();
	}

	public List<ViewInfo> getList()	{
		return viewList;
	}

	public void refresh() {
		getData();
	}

	@Override
	protected void onRangeChanged(HasData<ViewInfo> display) {
		getData();

		int start = display.getVisibleRange().getStart();
		int end = start + display.getVisibleRange().getLength();
		end = end >= viewList.size() ? viewList.size() : end;
		List<ViewInfo> sub = viewList.subList(start, end);
		updateRowData(start, sub);

	}

	private void getData() {
		if (schema != null) {
			studioService.getList(PgStudio.getToken(), schema.getId(),
					ITEM_TYPE.VIEW, new AsyncCallback<String>() {
						public void onFailure(Throwable caught) {
							viewList.clear();
							// Show the RPC error message to the user
							Window.alert(caught.getMessage());
						}

						public void onSuccess(String result) {
							viewList = new ArrayList<ViewInfo>();

							JsArray<ViewsJsObject> views = json2Messages(result);

							if (views != null) {
								viewList.clear();

								for (int i = 0; i < views.length(); i++) {
									ViewsJsObject view = views.get(i);
									viewList.add(msgToColumnInfo(view));
								}
							}

							updateRowCount(viewList.size(), true);
							updateRowData(0, viewList);

						}
					});
		}
	}

	private ViewInfo msgToColumnInfo(ViewsJsObject msg) {
		int id = Integer.parseInt(msg.getId());

		ViewInfo view = new ViewInfo(schema.getId(), id, msg.getName());

		view.setMaterialized(msg.getIsMaterialized().equalsIgnoreCase("true"));
		view.setComment(msg.getComment());

		return view;
	}

	private static final native JsArray<ViewsJsObject> json2Messages(String json)
	/*-{
		return eval(json);
	}-*/;

}