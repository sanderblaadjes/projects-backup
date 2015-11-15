package co.uk.android.lldc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import co.uk.android.lldc.async.DownloadFileAsync;
import co.uk.android.lldc.async.ExtractAsyntsk;
import co.uk.android.lldc.models.DashboardModel;
import co.uk.android.lldc.models.FacilityModel;
import co.uk.android.lldc.models.MediaModel;
import co.uk.android.lldc.models.ServerModel;
import co.uk.android.lldc.models.TheParkModel;
import co.uk.android.lldc.models.ThingsToDoModel;
import co.uk.android.lldc.models.TrailsWayPointModel;
import co.uk.android.lldc.tablet.HomeActivity;
import co.uk.android.lldc.tablet.LLDCApplication;
import co.uk.android.lldc.tablet.SplashScreen;

@SuppressWarnings("deprecation")
public class LLDC_Sync_Servcie extends IntentService {

	int count = 0;

	public LLDC_Sync_Servcie() {
		super("LLDC_Sync_Servcie");
	}

	Context mContext;

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		mContext = this.getApplicationContext();
		if (LLDCApplication.isConnectingToInternet(mContext)) {
			getData();
			LLDCApplication.setServerTime(mContext, Calendar.getInstance()
					.getTimeInMillis() + "");
			if (SplashScreen.mHandler != null)
				SplashScreen.mHandler.sendEmptyMessageDelayed(1005, 500);
			else if (HomeActivity.mHomeActivityHandler != null) {
				HomeActivity.mHomeActivityHandler.sendEmptyMessageDelayed(1006,
						LLDCApplication.refershInterval);
			}
		}
	}

	public void getData() {
		try {

			// android.os.Debug.waitForDebugger();
			DashboardModel model = new DashboardModel();

			JSONObject obj = null;

			String jsonResponse = "";

			if (LLDCApplication.isDebug)
				jsonResponse = getJSONFromUrl(LLDCApplication.DEVGETBASEDATA);
			else
				jsonResponse = getJSONFromUrl(LLDCApplication.PRODGETBASEDATA);
			/** CHange in next release */

			// jsonResponse = loadJSONFromAsset();

			if (jsonResponse == null)
				return;

			obj = new JSONObject(jsonResponse);

			if (obj != null & obj.length() > 0) {

				if (LLDCApplication.DBHelper == null)
					LLDCApplication.init(mContext);

				model.setWelcomeMsgIn(obj.getString("welcomeMsg").toString()
						.trim());
				// model.setWelcomeMsgOut(obj.getString("welcomeMsgOut").toString().trim());
				model.setWelcomeImageIn(obj.getString("welcomeImageIn")
						.toString().trim());
				model.setWelcomeImageOut(obj.getString("welcomeImageOut")
						.toString().trim());
				model.setMajorNotice(obj.getString("majorNotification")
						.toString().trim());
				model.setMinorNotice(obj.getString("minorNotification")
						.toString().trim());
				model.setTodaysDate(obj.getString("todaysDate").toString());

				model.setSocialUnavailMsg(obj.getString("socialUnavailMsg")
						.toString());

				model.setReportEmailTo(obj.getString("reportEmailTo")
						.toString());

				model.setReportEmailSubject(obj.getString("reportEmailSubject")
						.toString());

				model.setNelat(obj.getString("neLat"));
				model.setNelong(obj.getString("neLong"));
				model.setSwlat(obj.getString("swLat"));
				model.setSwlong(obj.getString("swLong"));

				LLDCApplication.noSocialMessage = obj.getString(
						"socialUnavailMsg").toString();
				String lat = obj.getString("parkLat").toString();
				String lan = obj.getString("parkLong").toString();

				model.setParkLat(lat);
				model.setParkLon(lan);

				LLDCApplication.parkCenter.setLatitude(Double.parseDouble(lat));
				LLDCApplication.parkCenter
						.setLongitude(Double.parseDouble(lan));

				ArrayList<ServerModel> eventList = parseModels(
						obj.getJSONArray("event"), LLDCApplication.EVENT);

				ArrayList<ThingsToDoModel> thingsList = parseThingsToDo(obj
						.getJSONArray("thingstodo"));

				ArrayList<TheParkModel> theParkList = parseTheParkModel(obj
						.getJSONArray("thePark"));

				LLDCApplication.DBHelper.onClearStaticTrailTable();
				LLDCApplication.DBHelper.onClearTrailWaypointTable();

				JSONObject jo_inside = obj.getJSONObject("boatTrails");
				ArrayList<String> tempTrails = new ArrayList<String>();
				tempTrails.add("1");
				tempTrails.add(jo_inside.getString("title"));
				tempTrails.add(jo_inside.getString("description"));
				tempTrails.add(jo_inside.getString("image"));

				LLDCApplication.DBHelper.onInsertStaticTrailsData(tempTrails);

				tempTrails.clear();

				jo_inside = obj.getJSONObject("guidedTours");

				tempTrails.add("2");
				tempTrails.add(jo_inside.getString("title"));
				tempTrails.add(jo_inside.getString("description"));
				tempTrails.add(jo_inside.getString("image"));

				jo_inside = obj.getJSONObject("mapfile");
				LLDCApplication.mapTileFileURL = jo_inside
						.getString("filePath");
				LLDCApplication.mapTileFileName = jo_inside
						.getString("fileNmae").toString().trim()
						.replace(".zip", "");
				LLDCApplication.setMapTileNameFromCMS(mContext,
						LLDCApplication.mapTileFileName);
				LLDCApplication.setMapTileURLFromCMS(mContext,
						LLDCApplication.mapTileFileURL);

				LLDCApplication.DBHelper.onInsertStaticTrailsData(tempTrails);

				JSONObject m_jObject = obj.getJSONObject("explore");

				ArrayList<ServerModel> venueList = parseModels(
						m_jObject.getJSONArray("venues"), LLDCApplication.VENUE);
				ArrayList<ServerModel> facilitiesList = parseModels(
						m_jObject.getJSONArray("facilities"),
						LLDCApplication.FACILITIES);
				ArrayList<ServerModel> trailsList = parseModels(
						m_jObject.getJSONArray("trails"),
						LLDCApplication.TRAILS);

				// LLDCApplication.dashBoardData = model;

				LLDCApplication.DBHelper.onClearDashboardTable();
				LLDCApplication.DBHelper.onInsertDashboardData(model);

				LLDCApplication.DBHelper.onClearMediaTable();

				LLDCApplication.DBHelper.onClearEventTable();
				LLDCApplication.DBHelper.onInsertEventData(eventList);

				LLDCApplication.DBHelper.onClearThingsToDoTable();
				LLDCApplication.DBHelper.onInsertThingsToDO(thingsList);

				LLDCApplication.DBHelper.onClearTheParkTable();
				LLDCApplication.DBHelper.onInsertThePark(theParkList);

				LLDCApplication.DBHelper.onClearVenuesTable();
				LLDCApplication.DBHelper.onClearVenueFacilityTable();
				LLDCApplication.DBHelper.onInsertVenuesData(venueList);

				LLDCApplication.DBHelper.onClearFacilitiesTable();
				LLDCApplication.DBHelper.onInsertFacilitiesData(facilitiesList);

				LLDCApplication.DBHelper.onClearTrailsTable();
				LLDCApplication.DBHelper.onInsertTrailsData(trailsList);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<ServerModel> parseModels(JSONArray m_jArry, int modelType) {
		ArrayList<ServerModel> eventList = new ArrayList<ServerModel>();
		for (int i = 0; i < m_jArry.length(); i++) {
			try {
				ServerModel item = new ServerModel();
				JSONObject jo_inside = m_jArry.getJSONObject(i);
				item.set_id(jo_inside.getString("id"));
				item.setName(jo_inside.getString("name"));
				item.setShortDesc(jo_inside.getString("shortDesc"));
				item.setLongDescription(jo_inside.getString("longDescription"));
				item.setVenueId(jo_inside.getString("venueId"));
				item.setVenueTitle(jo_inside.getString("venueTitle"));
				item.setLatitude(jo_inside.getString("lat"));
				item.setLongitude(jo_inside.getString("long"));
				item.setThumbImage(jo_inside.getString("thumbImage"));
				// Give to Image Cache Manager
				item.setLargeImage(jo_inside.getString("largeImage"));
				item.setFlag(jo_inside.getString("flag"));
				item.setIsToday(jo_inside.getString("isToday"));
				item.setStartDateTime(jo_inside.getString("startDateTime"));
				item.setEndDateTime(jo_inside.getString("endDateTime"));
				item.setActiveDays(jo_inside.getString("activeDays"));
				item.setCategory(jo_inside.getString("category"));
				if (jo_inside.getString("color").toString().equals("")) {
					item.setColor(LLDCApplication.EVENT_NAME_DEFAULT_COLOR);
				} else {
					item.setColor(jo_inside.getString("color"));
				}
				item.setOrder(jo_inside.getString("order"));
				item.setIsLondon2012(jo_inside.getInt("isLondon2012"));
				item.setModelType(modelType);
				JSONArray mMediaArray = jo_inside.getJSONArray("media");
				ArrayList<MediaModel> mediaList = new ArrayList<MediaModel>();
				for (int j = 0; j < mMediaArray.length(); j++) {
					MediaModel model2 = new MediaModel();
					model2.setImageUrl(mMediaArray.getJSONObject(j).getString(
							"imageLink"));
					// if (LLDCApplication.bImagePreFetch) {
					// count++;
					// LLDCApplication.onShowLogCat("ImageGiven",
					// "Image loader hit " + count);
					// ImageLoader.getInstance().loadImageSync(
					// mMediaArray.getJSONObject(j).getString(
					// "imageLink"));
					// }
					mediaList.add(model2);
				}

				if (modelType == LLDCApplication.TRAILS) {
					item.setDistance(jo_inside.getString("distance"));
					item.setDuration(jo_inside.getString("duration"));
					item.setRouteId(jo_inside.getString("routeId"));
					item.setStartTrailLat(jo_inside.getString("startTrailLat"));
					item.setStartTrailLong(jo_inside
							.getString("startTrailLong"));
					item.setEndTrailLat(jo_inside.getString("endTrailLat"));
					item.setEndTrailLong(jo_inside.getString("endTrailLong"));
					mMediaArray = jo_inside.getJSONArray("waypoints");

					ArrayList<TrailsWayPointModel> waypointList = new ArrayList<TrailsWayPointModel>();
					for (int j = 0; j < mMediaArray.length(); j++) {
						TrailsWayPointModel model2 = new TrailsWayPointModel();
						model2.setPinTitle(mMediaArray.getJSONObject(j)
								.getString("pinTitle"));
						model2.setPinImage(mMediaArray.getJSONObject(j)
								.getString("pinImage"));
						model2.setPinLat(mMediaArray.getJSONObject(j)
								.getString("pinLat"));
						model2.setPinLong(mMediaArray.getJSONObject(j)
								.getString("pinLong"));
						model2.setDescription(mMediaArray.getJSONObject(j)
								.getString("description"));
						model2.setTailsId(jo_inside.getString("id"));
						model2.setQueryString(mMediaArray.getJSONObject(j)
								.getString("queryString"));
						model2.setRouteNumber(mMediaArray.getJSONObject(j)
								.getString("routeNumber"));

						waypointList.add(model2);
					}

					item.setWyapoitnList(waypointList);
				}

				if (modelType == LLDCApplication.EVENT) {
					item.setSocialFlag(jo_inside.getString("socialFlag"));
					item.setSocialHandle(jo_inside.getString("socialHandle"));
					item.setExcludeFromSearch(jo_inside
							.getString("excludeFromSearch"));
				}

				if (modelType == LLDCApplication.VENUE) {
					mMediaArray = jo_inside.getJSONArray("facilities");
					item.setEventCount(Integer.parseInt(jo_inside
							.getString("count")));
					ArrayList<FacilityModel> venueFacilityList = parseVenueFacilities(
							mMediaArray, item.get_id(), LLDCApplication.VENUE);
					item.setVenueFacilityList(venueFacilityList);
				}

				if (modelType == LLDCApplication.FACILITIES) {
					mMediaArray = jo_inside.getJSONArray("facilities");
					ArrayList<FacilityModel> venueFacilityList = parseVenueFacilities(
							mMediaArray, item.get_id(),
							LLDCApplication.FACILITIES);
					item.setVenueFacilityList(venueFacilityList);
				}

				item.setMediaList(mediaList);
				LLDCApplication.onShowLogCat("Service", "parsed " + i);
				eventList.add(item);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				if (LLDCApplication.isDebug) {
					LLDCApplication.onShowToastMesssage(
							mContext,
							"Json Error on modelType: " + modelType + " "
									+ e.getMessage());
				}
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				if (LLDCApplication.isDebug) {
					LLDCApplication.onShowToastMesssage(
							mContext,
							"Error on modelType: " + modelType + " "
									+ e.getMessage());
				}
			}
		}

		return eventList;

	}

	public ArrayList<ThingsToDoModel> parseThingsToDo(JSONArray m_jArry) {
		ArrayList<ThingsToDoModel> eventList = new ArrayList<ThingsToDoModel>();
		try {
			for (int i = 0; i < m_jArry.length(); i++) {
				ThingsToDoModel item = new ThingsToDoModel();
				JSONObject jo_inside = m_jArry.getJSONObject(i);
				item.setId(jo_inside.getString("id"));
				item.setName(jo_inside.getString("name"));
				item.setText(jo_inside.getString("text"));
				item.setType(jo_inside.getString("type"));
				item.setSortOrder(jo_inside.getString("sortOrder"));
				item.setDesc(jo_inside.getString("description"));
				item.setImageURL(jo_inside.getString("imageURL"));
				item.setColor(jo_inside.getString("color"));
				LLDCApplication.onShowLogCat("Service", "parsed " + i);
				eventList.add(item);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return eventList;

	}

	public ArrayList<TheParkModel> parseTheParkModel(JSONArray m_jArry) {
		ArrayList<TheParkModel> parkList = new ArrayList<TheParkModel>();
		try {

			for (int i = 0; i < m_jArry.length(); i++) {
				TheParkModel item = new TheParkModel();
				JSONObject jo_inside = m_jArry.getJSONObject(i);
				item.setId(jo_inside.getString("id"));
				item.setTitle(jo_inside.getString("title"));
				item.setSubTitle(jo_inside.getString("subTitle"));
				item.setDesc(jo_inside.getString("longDescription"));
				item.setImageUrl(jo_inside.getString("images"));
				LLDCApplication.onShowLogCat("Service", "parsed " + i);
				parkList.add(item);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return parkList;
	}

	public ArrayList<FacilityModel> parseVenueFacilities(JSONArray m_jArry,
			String id, int modelType) {
		ArrayList<FacilityModel> venueFacilityList = new ArrayList<FacilityModel>();
		try {

			for (int i = 0; i < m_jArry.length(); i++) {
				FacilityModel item = new FacilityModel();
				JSONObject jo_inside = m_jArry.getJSONObject(i);
				item.setId(id);
				item.setFacility_id(jo_inside.getString("id"));
				item.setLat(jo_inside.getString("lat"));
				item.setLon(jo_inside.getString("long"));
				item.setModelType(modelType);
				LLDCApplication.onShowLogCat("Service", "parsed " + i);
				venueFacilityList.add(item);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return venueFacilityList;
	}

	public String loadJSONFromAsset() {
		String json = null;
		try {

			InputStream is = mContext.getAssets().open("basedata.json");

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);

			is.close();

			json = new String(buffer, "UTF-8");

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
		return json;

	}

	public String getJSONFromUrl(String url) {
		// Making HTTP request

		InputStream is = null;
		String json = null;

		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			if (sb.toString() != null && !sb.toString().equals(""))
				json = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
		// return JSON String
		return json;
	}

	private void DownloadZipFile() {
		// String[] url = {""};
		DownloadFileAsync tempDownlaod = (DownloadFileAsync) new DownloadFileAsync(
				mContext, "", "");
		tempDownlaod.execute();
	}

	private void onExtractIntitalData() {
		new ExtractAsyntsk(mContext, "").execute();
	}

}
