package com.eldoheiri.realtime_analytics.apiresources;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eldoheiri.datastore.DataStore;
import com.eldoheiri.realtime_analytics.dataaccess.DataSource;
import com.eldoheiri.realtime_analytics.dataaccess.dataobjects.ApplicationEvent;
import com.eldoheiri.realtime_analytics.dataaccess.dataobjects.Device;
import com.eldoheiri.realtime_analytics.dataaccess.dataobjects.Session;
import com.eldoheiri.realtime_analytics.dataobjects.DeviceDTO;
import com.eldoheiri.realtime_analytics.dataobjects.SessionDTO;
import com.eldoheiri.realtime_analytics.dataobjects.events.ApplicationEventDTO;
import com.eldoheiri.realtime_analytics.dataobjects.events.HeartBeatDTO;
import com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.DeviceException;
import com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.heartbeat.HeartBeatException;
import com.eldoheiri.realtime_analytics.exceptionhandling.Exceptions.session.SessionException;
import com.eldoheiri.realtime_analytics.security.authentication.JWTUtil;

import jakarta.validation.Valid;

import java.sql.Connection;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/v1/{applicationId}")
public class SessionResource {

    @Autowired
    private JWTUtil jwtUtil;

    @PostMapping("/device")
    public DeviceDTO newDevice(@Valid @RequestBody DeviceDTO deviceDTO, @PathVariable Integer applicationId) {
        try (Connection dbConnection = DataSource.getConnection()) {
            DataStore dataStore = new DataStore();
            Device device = new Device();
            device.setModel(deviceDTO.getModel());
            device.setApplicationId(applicationId);
            dataStore.insert(device, dbConnection);
            dbConnection.commit();

            deviceDTO.setId(device.getId());
            deviceDTO.setApplicationId(applicationId);
            return deviceDTO;
        } catch (IllegalArgumentException | SQLException e) {
            e.printStackTrace();
            throw new DeviceException(e);
        } catch (DeviceException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    

    @PostMapping("/session")
    public SessionDTO newSession(@Valid @RequestBody SessionDTO sessionRequest, @PathVariable Integer applicationId) {
        try (Connection dbConnection = DataSource.getConnection()) {

            DataStore dataStore = new DataStore();
            Session session = new Session();
            session.setApplicationId(applicationId);
            session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            session.setDeviceId(sessionRequest.getDeviceId());
            List<ApplicationEvent> applicationEvents = createApplicationEvents(sessionRequest.getHeartBeat(), null);
            ApplicationEvent heartBeat = createHeartBeatEvent(sessionRequest.getHeartBeat(), null);
            if (heartBeat != null) {
                applicationEvents.add(heartBeat);
            }
            session.setEvents(applicationEvents);
            dataStore.insert(session, dbConnection);

            dbConnection.commit();

            SessionDTO response = new SessionDTO();
            response.setId(session.getId());
            response.setApplicationId(applicationId);
            response.setToken(jwtUtil.generateTokenString(session.getId().toString()));
            response.setDeviceId(sessionRequest.getDeviceId());
            response.setCreateAt(session.getCreatedAt());
            if (sessionRequest.getHeartBeat() != null && heartBeat != null) {
                response.setHeartBeat(sessionRequest.getHeartBeat());
                response.getHeartBeat().setId(heartBeat.getId());
            }
            if (sessionRequest.getHeartBeat() != null && sessionRequest.getHeartBeat().getEvents() != null) {
                for (int i = 0; i < sessionRequest.getHeartBeat().getEvents().size(); i++) {
                    response.getHeartBeat().getEvents().get(i).setId(applicationEvents.get(i).getId());
                }
            }
            return response;
        } catch (IllegalArgumentException | SQLException e) {
            e.printStackTrace();
            throw new SessionException(e);
        } catch (SessionException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    

    @PostMapping("/session/{sessionId}/heartBeat")
    public HeartBeatDTO heartBeat(@Valid @RequestBody HeartBeatDTO sessionHeartBeat, @PathVariable Integer sessionId) {
        try (Connection dbConnection = DataSource.getConnection()) {
            HeartBeatDTO result = insert(sessionHeartBeat, sessionId, dbConnection);
            dbConnection.commit();
            return result;
        } catch (IllegalArgumentException | SQLException e) {
            e.printStackTrace();
            throw new HeartBeatException(e);
        } catch (HeartBeatException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private HeartBeatDTO insert(HeartBeatDTO sessionHeartBeat, Integer sessionId, Connection dbConnection) throws SQLException {
        DataStore dataStore = new DataStore();
        ApplicationEvent heartBeat = createHeartBeatEvent(sessionHeartBeat, sessionId);
        List<ApplicationEvent> applicationEvents = createApplicationEvents(sessionHeartBeat, sessionId);
        dataStore.insert(heartBeat, dbConnection);
        dataStore.insert(applicationEvents, dbConnection);
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
        if (dto == null) {
            return null;
        }
        var heartBeat = new ApplicationEvent();
        heartBeat.setSessionId(sessionId);
        heartBeat.setTimestamp(dto.getTimestamp());
        heartBeat.setPayload(dto.getAttributes());
        heartBeat.setType("heart_beat");
        return heartBeat;
    }

    private List<ApplicationEvent> createApplicationEvents(HeartBeatDTO heartBeat, Integer sessionId) {
        if (heartBeat == null || heartBeat.getEvents() == null) {
            return new ArrayList<>();
        }
        ArrayList<ApplicationEvent> result = new ArrayList<>();
        for (ApplicationEventDTO dto : heartBeat.getEvents()) {
            var applicationEvent = new ApplicationEvent();
            applicationEvent.setPayload(dto.getAttributes());
            applicationEvent.setSessionId(sessionId);
            applicationEvent.setTimestamp(dto.getTimestamp());
            applicationEvent.setType(dto.getType().name());
            result.add(applicationEvent);
        }
        return result;
    }
}
