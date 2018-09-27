package com.ca.apm.tests.utils;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.classes.from.appmap.plugin.AtFilter;
import com.ca.apm.classes.from.appmap.plugin.AtStory;
import com.ca.apm.classes.from.appmap.plugin.AtStoryList;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchATStories {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private RestClient restClient = new RestClient();
    private Common common = new Common();
    private static final ObjectMapper mapper = new ObjectMapper();

    public AtStoryList fetchStories(String urlPart, AtFilter filter) throws JsonParseException,
        JsonMappingException, IOException {

        EmRestRequest request = new EmRestRequest(urlPart, mapper.writeValueAsString(filter));
        IRestResponse<String> response = restClient.process(request);
        String jsonResponse = response.getContent();

        // Initialize stories through Jackson
        AtStoryList stories = mapper.readValue(jsonResponse, AtStoryList.class);
        return stories;
    }

    public AtStoryList fetchStories(String urlPart, Timestamp start_time, Timestamp end_time,
        boolean merged) throws IOException {

        Set<Integer> storyIds = new HashSet<Integer>();
        AtFilter filter = new AtFilter();
        filter.setEndTime(common.timestamp2String(end_time));
        filter.setStartTime(common.timestamp2String(start_time));
        String postPayload = mapper.writeValueAsString(filter);
        log.info("post payload for URL:{} payload:{}",urlPart,postPayload);
        EmRestRequest request = new EmRestRequest(urlPart, postPayload);
        IRestResponse<String> response = restClient.process(request);
        String jsonResponse = response.getContent();
        log.info("Response for URL:{} reponse:{}",urlPart,jsonResponse);

        // Initialize stories through Jackson
        AtStoryList stories = mapper.readValue(jsonResponse, AtStoryList.class);

        if (stories.getStories() != null && stories.getStories().isEmpty()) {
            fail("No new stories created for payload" + postPayload);
        } else {
            Iterator<AtStory> itStory = stories.getStories().iterator();
            while (itStory.hasNext()) {
                AtStory storyLocation = itStory.next();
                Set<Integer> ids = convertToIntegerSet(storyLocation.getStoryIds());

                if (ids.isEmpty()) {
                    log.error("Missing storyIds. Is it available in response for given payload?");
                } else {
                    storyIds.addAll(ids);
                }
            }
        }
        
        if(storyIds.isEmpty()){
            return new AtStoryList();
        }else{
            return fetchStoriesDetailed(urlPart, start_time, end_time, storyIds, merged);
        }
    }

    public AtStoryList fetchStoriesDetailed(String urlPart, Timestamp start_time,
        Timestamp end_time, Integer storyId, boolean merged) throws IOException {
        return fetchStoriesDetailed(urlPart, start_time, end_time,
            new HashSet<Integer>(Arrays.asList(storyId)), merged);
    }

    public AtStoryList fetchStoriesDetailed(String urlPart, Timestamp start_time,
        Timestamp end_time, Set<Integer> storyIds, boolean merged) throws IOException {

        AtFilter filter = new AtFilter();
        filter.setStartTime(common.timestamp2String(start_time));
        filter.setEndTime(common.timestamp2String(end_time));
        filter.setStoryIds(convertToStringSet(storyIds));
        filter.setProjection(AtFilter.PROJ_DETAILED);
        filter.setMergeStories(merged);
        filter.setLimit(-1);
        
        String postPayload = mapper.writeValueAsString(filter);
        log.info("post payload for URL:{} payload:{}",urlPart,postPayload);
        EmRestRequest request = new EmRestRequest(urlPart, postPayload);
        IRestResponse<String> response = restClient.process(request);
        String returnValue = response.getContent();
        log.info("Response for URL:{} payload:{} response:{}",urlPart,postPayload,returnValue);
        
        return mapper.readValue(returnValue, AtStoryList.class);
    }


    public Set<Integer> convertToIntegerSet(Set<String> set) {
        HashSet<Integer> strs = new HashSet<Integer>(set.size());
        for (String str : set) {
            str = str.split(":")[0];
            strs.add(Integer.parseInt(str));
        }
        return strs;


    }


    public Set<String> convertToStringSet(Set<Integer> set) {
        HashSet<String> strs = new HashSet<String>(set.size());
        for (Integer integer : set)
            strs.add(integer.toString());

        return strs;


    }


}
