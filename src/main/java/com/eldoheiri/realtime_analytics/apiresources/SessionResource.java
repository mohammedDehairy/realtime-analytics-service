package com.eldoheiri.realtime_analytics.apiresources;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eldoheiri.datastore.DataStore;
import com.eldoheiri.realtime_analytics.dataaccess.dataobjects.ApplicationEvent;
import com.eldoheiri.realtime_analytics.dataaccess.dataobjects.Session;
import com.eldoheiri.realtime_analytics.dataobjects.HeartBeatDTO;
import com.eldoheiri.realtime_analytics.dataobjects.SessionDTO;
import com.eldoheiri.realtime_analytics.dataobjects.events.ApplicationEventDTO;
import com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.session.SessionException;

@RestController
@RequestMapping("/api/v1/{applicationId}/session")
public class SessionResource {

    @PostMapping
    public SessionDTO newSession(@RequestBody HeartBeatDTO sessionHeartBeat, @PathVariable Integer applicationId) {
        try {
            DataStore dataStore = new DataStore();

            Session session = new Session();
            session.setApplicationId(applicationId);
            dataStore.insert(session);

            insert(sessionHeartBeat, session.getId());

            SessionDTO response = new SessionDTO();
            response.setApplicationId(session.getApplicationId());
            response.setId(session.getId());
            return response;
        } catch (IllegalArgumentException | SQLException e) {
            throw new SessionException(e);
        } catch (SessionException e) {
            throw e;
        }
    }
    

    @PostMapping("/{sessionId}/heartBeat")
    public HeartBeatDTO heartBeat(@RequestBody HeartBeatDTO sessionHeartBeat, @PathVariable Integer sessionId) {
        try {
            return insert(sessionHeartBeat, sessionId);
        } catch (IllegalArgumentException | SQLException e) {
            throw new SessionException(e);
        } catch (SessionException e) {
            throw e;
        }
    }

    private HeartBeatDTO insert(HeartBeatDTO sessionHeartBeat, Integer sessionId) throws SQLException {
        DataStore dataStore = new DataStore();
        ApplicationEvent heartBeat = createHeartBeatEvent(sessionHeartBeat, sessionId);
        List<ApplicationEvent> applicationEvents = createApplicationEvents(sessionHeartBeat, sessionId);
        dataStore.insert(heartBeat);
        dataStore.insert(applicationEvents);
        sessionHeartBeat.setId(heartBeat.getId());
        if (sessionHeartBeat.getEvents() == null || sessionHeartBeat.getEvents().isEmpty()) {
            return sessionHeartBeat;
        }
        for (int i = 0; i < sessionHeartBeat.getEvents().size(); i++) {
            sessionHeartBeat.getEvents().get(i).setId(applicationEvents.get(i).getId());
        }
        return sessionHeartBeat;
    }

    private ApplicationEvent createHeartBeatEvent(HeartBeatDTO dto, Integer sessionId) {
        var heartBeat = new ApplicationEvent();
        heartBeat.setSessionId(sessionId);
        heartBeat.setTimestamp(dto.getTimestamp());
        heartBeat.setPayload(dto.getAttributes());
        heartBeat.setType("heart_beat");
        return heartBeat;
    }

    private List<ApplicationEvent> createApplicationEvents(HeartBeatDTO heartBeat, Integer sessionId) {
        if (heartBeat.getEvents() == null) {
            return List.of();
        }
        ArrayList<ApplicationEvent> result = new ArrayList<>();
        for (ApplicationEventDTO dto : heartBeat.getEvents()) {
            var applicationEvent = new ApplicationEvent();
            applicationEvent.setPayload(dto.getAttributes());
            applicationEvent.setSessionId(sessionId);
            applicationEvent.setTimestamp(dto.getTimestamp());
            applicationEvent.setType(dto.getType());
            result.add(applicationEvent);
        }
        return result;
    }
}
