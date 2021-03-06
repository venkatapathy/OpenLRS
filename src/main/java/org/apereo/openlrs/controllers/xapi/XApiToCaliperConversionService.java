/**
 * 
 */
package org.apereo.openlrs.controllers.xapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.openlrs.model.event.Actor;
import org.apereo.openlrs.model.event.Event;
import org.apereo.openlrs.model.event.Generated;
import org.apereo.openlrs.model.event.Group;
import org.apereo.openlrs.model.event.Object;
import org.apereo.openlrs.model.event.Result;
import org.apereo.openlrs.model.event.SubOrganizationOf;
import org.apereo.openlrs.model.xapi.Statement;
import org.apereo.openlrs.model.xapi.XApiAccount;
import org.apereo.openlrs.model.xapi.XApiActor;
import org.apereo.openlrs.model.xapi.XApiContext;
import org.apereo.openlrs.model.xapi.XApiContextActivities;
import org.apereo.openlrs.model.xapi.XApiObject;
import org.apereo.openlrs.model.xapi.XApiObjectDefinition;
import org.apereo.openlrs.model.xapi.XApiResult;
import org.apereo.openlrs.model.xapi.XApiVerb;
import org.imsglobal.caliper.actions.Action;
import org.imsglobal.caliper.context.Context;
import org.imsglobal.caliper.entities.DigitalResourceType;
import org.imsglobal.caliper.entities.EntityType;
import org.imsglobal.caliper.entities.Type;
import org.imsglobal.caliper.events.EventType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component
public class XApiToCaliperConversionService {
  
  private BidiMap<Action, String> verbActionMap;
  private BidiMap<Type, String> objectEntityMap;
  private Map<Action, EventType> actionEventMap;
  
  @PostConstruct
  private void init() {
    verbActionMap = new DualHashBidiMap<Action, String>();
    verbActionMap.put(Action.ABANDONED, "https://w3id.org/xapi/adl/verbs/abandoned");
    //verbActionMap.put(Action.ACTIVATED, );
    verbActionMap.put(Action.ATTACHED, "http://activitystrea.ms/schema/1.0/attach");    
    verbActionMap.put(Action.BOOKMARKED, "http://id.tincanapi.com/verb/bookmarked");
    //verbActionMap.put(Action.CHANGED_RESOLUTION, );
    //verbActionMap.put(Action.CHANGED_SIZE, );
    //verbActionMap.put(Action.CHANGED_VOLUME, );
    //verbActionMap.put(Action.CLASSIFIED, );
    //verbActionMap.put(Action.CLOSED_POPOUT, );
    verbActionMap.put(Action.COMMENTED, "http://adlnet.gov/expapi/verbs/commented");
    verbActionMap.put(Action.COMPLETED, "http://adlnet.gov/expapi/verbs/completed");
    //verbActionMap.put(Action.DEACTIVATED, );
    //verbActionMap.put(Action.DESCRIBED, );
    verbActionMap.put(Action.DISLIKED, "http://activitystrea.ms/schema/1.0/dislike");
    //verbActionMap.put(Action.DISABLED_CLOSED_CAPTIONING, );
    //verbActionMap.put(Action.ENABLED_CLOSED_CAPTIONING, );
    //verbActionMap.put(Action.ENDED, );
    //verbActionMap.put(Action.ENTERED_FULLSCREEN, );
    //verbActionMap.put(Action.EXITED_FULLSCREEN, );
    //verbActionMap.put(Action.FORWARDED_TO, );
    verbActionMap.put(Action.GRADED, "http://adlnet.gov/expapi/verbs/scored");
    //verbActionMap.put(Action.HID, );
    //verbActionMap.put(Action.HIGHLIGHTED, );
    //verbActionMap.put(Action.JUMPED_TO, );
    //verbActionMap.put(Action.IDENTIFIED, );
    verbActionMap.put(Action.LIKED, "http://activitystrea.ms/schema/1.0/like");
    //verbActionMap.put(Action.LINKED, );
    
    // alternatives
    // https://w3id.org/xapi/adl/verbs/logged-in
    // https://w3id.org/xapi/adl/verbs/logged-out
    
    verbActionMap.put(Action.LOGGED_IN, "https://brindlewaye.com/xAPITerms/verbs/loggedin/");
    verbActionMap.put(Action.LOGGED_OUT, "https://brindlewaye.com/xAPITerms/verbs/loggedout/");
    
    //verbActionMap.put(Action.MUTED, );
    //verbActionMap.put(Action.NAVIGATED_TO, );
    //verbActionMap.put(Action.OPENED_POPOUT, );
    verbActionMap.put(Action.PAUSED, "http://id.tincanapi.com/verb/paused");
    //verbActionMap.put(Action.RANKED, );
    verbActionMap.put(Action.QUESTIONED, "http://adlnet.gov/expapi/verbs/asked");
    //verbActionMap.put(Action.RECOMMENDED, );
    verbActionMap.put(Action.REPLIED, "http://adlnet.gov/expapi/verbs/responded");
    //verbActionMap.put(Action.RESTARTED, );
    verbActionMap.put(Action.RESUMED, "http://adlnet.gov/expapi/verbs/resumed");
    verbActionMap.put(Action.REVIEWED, "http://id.tincanapi.com/verb/reviewed");
    //verbActionMap.put(Action.REWOUND, );
    verbActionMap.put(Action.SEARCHED, "http://activitystrea.ms/schema/1.0/search");
    verbActionMap.put(Action.SHARED, "http://activitystrea.ms/schema/1.0/share");
    //verbActionMap.put(Action.SHOWED, );
    verbActionMap.put(Action.SKIPPED, "http://id.tincanapi.com/verb/skipped");
    verbActionMap.put(Action.STARTED, "http://activitystrea.ms/schema/1.0/start");
    verbActionMap.put(Action.SUBMITTED, "http://activitystrea.ms/schema/1.0/submit");
    //verbActionMap.put(Action.SUBSCRIBED, );
    verbActionMap.put(Action.TAGGED, "http://activitystrea.ms/schema/1.0/tag");
    //verbActionMap.put(Action.TIMED_OUT, );
    verbActionMap.put(Action.VIEWED, "http://id.tincanapi.com/verb/viewed");
    //verbActionMap.put(Action.UNMUTED, );
    
    objectEntityMap = new DualHashBidiMap<Type, String>();
    // TODO support other xapi annotation types
    objectEntityMap.put(EntityType.ANNOTATION, "http://risc-inc.com/annotator/activities/highlight");
    //objectEntityMap.put(EntityType.ATTEMPT, arg1);
    //objectEntityMap.put(EntityType.COURSE_OFFERING, arg1);
    objectEntityMap.put(EntityType.COURSE_SECTION, "http://adlnet.gov/expapi/activities/course");
    objectEntityMap.put(EntityType.DIGITAL_RESOURCE, "http://adlnet.gov/expapi/activities/media");
    //objectEntityMap.put(EntityType.ENTITY, );
    objectEntityMap.put(EntityType.GROUP, "http://activitystrea.ms/schema/1.0/group");
    objectEntityMap.put(EntityType.LEARNING_OBJECTIVE, "http://adlnet.gov/expapi/activities/objective");
    //objectEntityMap.put(EntityType.MEMBERSHIP, arg1);
    objectEntityMap.put(EntityType.PERSON, "http://activitystrea.ms/schema/1.0/person");
    objectEntityMap.put(EntityType.ORGANIZATION, "http://activitystrea.ms/schema/1.0/organization");
    //objectEntityMap.put(EntityType.RESPONSE, arg1);
    //objectEntityMap.put(EntityType.RESULT, arg1);
    //objectEntityMap.put(EntityType.SESSION, arg1);
    objectEntityMap.put(EntityType.SOFTWARE_APPLICATION, "http://activitystrea.ms/schema/1.0/application");
    //objectEntityMap.put(EntityType.VIEW, arg1);
    //objectEntityMap.put(DigitalResourceType.MEDIA_LOCATION, arg1);
    objectEntityMap.put(DigitalResourceType.MEDIA_OBJECT, "http://adlnet.gov/expapi/activities/media");
    //objectEntityMap.put(DigitalResourceType.READING, arg1);
    objectEntityMap.put(DigitalResourceType.WEB_PAGE, "http://activitystrea.ms/schema/1.0/page");
    
    actionEventMap = new HashMap<Action,EventType>();
    actionEventMap.put(Action.ABANDONED, EventType.ASSIGNABLE);
    actionEventMap.put(Action.ACTIVATED, EventType.ASSIGNABLE);
    actionEventMap.put(Action.ATTACHED, EventType.ANNOTATION);    
    actionEventMap.put(Action.BOOKMARKED, EventType.ANNOTATION);
    actionEventMap.put(Action.CHANGED_RESOLUTION, EventType.MEDIA);
    actionEventMap.put(Action.CHANGED_SIZE, EventType.MEDIA);
    actionEventMap.put(Action.CHANGED_VOLUME, EventType.MEDIA);
    actionEventMap.put(Action.CLASSIFIED, EventType.ANNOTATION);
    actionEventMap.put(Action.CLOSED_POPOUT, EventType.MEDIA);
    actionEventMap.put(Action.COMMENTED, EventType.ANNOTATION);
    actionEventMap.put(Action.COMPLETED, EventType.ASSIGNABLE);
    actionEventMap.put(Action.DEACTIVATED, EventType.ASSIGNABLE);
    actionEventMap.put(Action.DESCRIBED, EventType.ANNOTATION);
    actionEventMap.put(Action.DISLIKED, EventType.ANNOTATION);
    actionEventMap.put(Action.DISABLED_CLOSED_CAPTIONING, EventType.MEDIA);
    actionEventMap.put(Action.ENABLED_CLOSED_CAPTIONING, EventType.MEDIA);
    actionEventMap.put(Action.ENDED, EventType.MEDIA);
    actionEventMap.put(Action.ENTERED_FULLSCREEN, EventType.MEDIA);
    actionEventMap.put(Action.EXITED_FULLSCREEN, EventType.MEDIA);
    actionEventMap.put(Action.FORWARDED_TO, EventType.MEDIA);
    actionEventMap.put(Action.GRADED, EventType.OUTCOME);
    actionEventMap.put(Action.HID, EventType.ASSIGNABLE);
    actionEventMap.put(Action.HIGHLIGHTED, EventType.ANNOTATION);
    actionEventMap.put(Action.JUMPED_TO, EventType.MEDIA);
    actionEventMap.put(Action.IDENTIFIED, EventType.ANNOTATION);
    actionEventMap.put(Action.LIKED, EventType.ANNOTATION);
    actionEventMap.put(Action.LINKED, EventType.ANNOTATION);
    actionEventMap.put(Action.LOGGED_IN, EventType.SESSION);
    actionEventMap.put(Action.LOGGED_OUT, EventType.SESSION);   
    actionEventMap.put(Action.MUTED, EventType.MEDIA);
    actionEventMap.put(Action.NAVIGATED_TO, EventType.NAVIGATION);
    actionEventMap.put(Action.OPENED_POPOUT, EventType.MEDIA);
    actionEventMap.put(Action.PAUSED, EventType.MEDIA);
    actionEventMap.put(Action.RANKED, EventType.ANNOTATION);
    actionEventMap.put(Action.QUESTIONED, EventType.ANNOTATION);
    actionEventMap.put(Action.RECOMMENDED, EventType.ANNOTATION);
    actionEventMap.put(Action.REPLIED, EventType.ANNOTATION);
    actionEventMap.put(Action.RESTARTED, EventType.ASSESSMENT);
    actionEventMap.put(Action.RESUMED, EventType.MEDIA);
    actionEventMap.put(Action.REVIEWED, EventType.ASSIGNABLE);
    actionEventMap.put(Action.REWOUND, EventType.MEDIA);
    actionEventMap.put(Action.SEARCHED, EventType.READING);
    actionEventMap.put(Action.SHARED, EventType.ANNOTATION);
    actionEventMap.put(Action.SHOWED, EventType.ASSIGNABLE);
    actionEventMap.put(Action.SKIPPED, EventType.ASSESSMENT_ITEM);
    actionEventMap.put(Action.STARTED, EventType.EVENT);
    actionEventMap.put(Action.SUBMITTED, EventType.EVENT);
    actionEventMap.put(Action.SUBSCRIBED, EventType.ANNOTATION);
    actionEventMap.put(Action.TAGGED, EventType.ANNOTATION);
    actionEventMap.put(Action.TIMED_OUT, EventType.SESSION);
    actionEventMap.put(Action.VIEWED, EventType.EVENT);
    actionEventMap.put(Action.UNMUTED, EventType.MEDIA);

  }

  public Event fromXapi(Statement statement) {
    
    // EVENT TIME
    DateTime eventTime = null;    
    String timestamp = statement.getTimestamp(); 
    if (StringUtils.isNotBlank(timestamp)) {
      eventTime = new DateTime(timestamp);
    }
    else {
      eventTime = new DateTime(DateTimeZone.UTC);
    }
    // EVENT TIME END
    
    // ACTOR
    Actor caliperActor = null;
    XApiActor xapiActor = statement.getActor();
    if (xapiActor != null) {
      
      String actorId = null;
      String actorType = null; 
      Map<String, String> actorExtensions = new HashMap<>();
      String actorName = xapiActor.getName();
      
      String openId = xapiActor.getOpenid();
      String mbox = xapiActor.getMbox();
      XApiAccount xapiAccount = xapiActor.getAccount();
      
      if (StringUtils.isNotBlank(openId)) {
        actorId = openId;
        actorType = ACTOR_TYPE_OPENID;
      }
      else if (StringUtils.isNotBlank(mbox)) {
        actorId = mbox;
        actorType = ACTOR_TYPE_MBOX;
      }
      else if (xapiAccount != null) {
        String accountName = xapiAccount.getName();
        String homePage = xapiAccount.getHomePage();
        
        if (StringUtils.isNotBlank(homePage)) {
          
          if (StringUtils.isNotBlank(accountName)) {
            actorId = accountName;
            actorExtensions.put("HOMEPAGE", homePage);
          }
          else {
            actorId = homePage;
          }
        }
        else if (StringUtils.isNotBlank(accountName)) {
          actorId = accountName;
        }
        
        actorType = ACTOR_TYPE_ACCOUNT;
      }
      
      caliperActor = new Actor(actorId, 
          Context.CONTEXT.getValue(), 
          actorType, 
          actorName, 
          null, 
          actorExtensions);
    }
    // ACTOR END
    
    // RESULT
    Result caliperResult = null;
    XApiResult xapiResult = statement.getResult();
    if (xapiResult != null) {
      Map<String,String> resultExtensions = null;
      Map<URI,java.lang.Object> xapiResultExtensions = xapiResult.getExtensions();
      if (xapiResultExtensions != null && !xapiResultExtensions.isEmpty()) {
        resultExtensions = new HashMap<>();
        for (Map.Entry<URI,java.lang.Object> entry : xapiResultExtensions.entrySet()) {
          resultExtensions.put(entry.getKey().toString(),entry.getValue().toString());
        }
      }
      
      caliperResult = new Result(UUID.randomUUID().toString(),
          Context.CONTEXT.getValue(),
          "http://purl.imsglobal.org/caliper/v1/Result",
          null,
          null,
          resultExtensions,
          caliperActor,
          null,
          null,
          null);
    }
    // END Result
    
    // ACTION
    String caliperAction = null;
    XApiVerb xapiVerb = statement.getVerb();
    if (xapiVerb != null) {
      String verbId = xapiVerb.getId();
      caliperAction = verbId;
    }
    // ACTION END
    
    // OBJECT
    Object caliperObject = null;
    XApiObject xapiObject = statement.getObject();
    if (xapiObject != null) {
      
      String objectType = null;
      String objectName = null;
      String objectDescription = null;
      Map<String,String> objectExtensions = null;
      String objectId = xapiObject.getId();
      
      XApiObjectDefinition xapiObjectDefinition = xapiObject.getDefinition();
      if (xapiObjectDefinition != null) {
        String xapiObjectDefinitionType = xapiObjectDefinition.getType();
        if (StringUtils.isNotBlank(xapiObjectDefinitionType)) {
          objectType = xapiObjectTypeToCaliperEntityType(xapiObjectDefinitionType);
        }
        
        Map<String,String> names = xapiObjectDefinition.getName();
        if (names != null) {
          if (names.size() == 1) {
            objectName = CollectionUtils.get(names, 0).getValue();
          }
          else {
            // default to en?
            objectName = names.get("en");
          }
        }
        
        Map<String,String> descriptions = xapiObjectDefinition.getDescription();
        if (descriptions != null) {
          if (descriptions.size() == 1) {
            objectDescription = CollectionUtils.get(descriptions, 0).getValue();
          }
          else {
            // default to en?
            objectDescription = descriptions.get("en");
          }
        }
        
        Map<URI,java.lang.Object> extensions = xapiObjectDefinition.getExtensions();
        if (extensions != null && !extensions.isEmpty()) {
          objectExtensions = new HashMap<String,String>(extensions.size());
          for (URI key : extensions.keySet()) {
            objectExtensions.put(key.toString(), extensions.get(key).toString());
          }
        }
      }
           
      caliperObject = new Object(objectId, 
          Context.CONTEXT.getValue(), 
          objectType, 
          objectName, 
          objectDescription, 
          objectExtensions);
    }
    // OBJECT END
    
    Group caliperGroup = null;
    XApiContext xapiContext = statement.getContext();
    if (xapiContext != null) {
      Map<String,String> contextExtensions = null;
      Map<URI,java.lang.Object> extensions = xapiContext.getExtensions();
      if (extensions != null && !extensions.isEmpty()) {
        contextExtensions = new HashMap<String,String>(extensions.size());
        for (URI key : extensions.keySet()) {
          contextExtensions.put(key.toString(), extensions.get(key).toString());
        }
      }

      XApiContextActivities xapiContextActivities = xapiContext.getContextActivities();
      if (xapiContextActivities != null) {
        xapiContextActivities.getParent();
        
        List<XApiObject> groupings = xapiContextActivities.getGrouping();
        // TODO - handle multiple groupings
        if (groupings != null && groupings.size() == 1) {
          XApiObject grouping = groupings.get(0);
          
          String id = grouping.getId();
          String type = null;
          String name = null;
          String description = null;
          XApiObjectDefinition xapiObjectDefinition = grouping.getDefinition();
          if (xapiObjectDefinition != null) {
            type = xapiObjectDefinition.getType();
            
            Map<String,String> names =xapiObjectDefinition.getName();
            if (names != null) {
              if (names.size() == 1) {
                name = CollectionUtils.get(names, 0).getValue();
              }
              else {
                // default to en?
                name = names.get("en");
              }
            }

            Map<String,String> descriptions = xapiObjectDefinition.getDescription();
            if (descriptions != null) {
              if (descriptions.size() == 1) {
                description = CollectionUtils.get(descriptions, 0).getValue();
              }
              else {
                // default to en?
                description = descriptions.get("en");
              }
            }
          }
          
          List<XApiObject> parents = xapiContextActivities.getParent();
          SubOrganizationOf subOrganizationOf = null;
          if (parents != null && parents.size() == 1) {
            XApiObject parent = parents.get(0);
            String parentId = parent.getId();
            String parentType = null;
            String parentName = null;
            String parentDescription = null;
            XApiObjectDefinition parentXapiObjectDefinition = parent.getDefinition();
            if (parentXapiObjectDefinition != null) {
              parentType = parentXapiObjectDefinition.getType();
              
              Map<String,String> names = parentXapiObjectDefinition.getName();
              if (names != null) {
                if (names.size() == 1) {
                  parentName = CollectionUtils.get(names, 0).getValue();
                }
                else {
                  // default to en?
                  parentName = names.get("en");
                }
              }

              Map<String,String> descriptions = parentXapiObjectDefinition.getDescription();
              if (descriptions != null) {
                if (descriptions.size() == 1) {
                  parentDescription = CollectionUtils.get(descriptions, 0).getValue();
                }
                else {
                  // default to en?
                  parentDescription = descriptions.get("en");
                }
              }

              subOrganizationOf = new SubOrganizationOf(parentId, 
                  Context.CONTEXT.getValue(), 
                  parentType, 
                  parentName, 
                  parentDescription, 
                  null, 
                  null, 
                  null, 
                  null, 
                  null);
            }
          }
          
          caliperGroup = new Group(id, 
              Context.CONTEXT.getValue(), 
              type, 
              name, 
              description, 
              contextExtensions, 
              subOrganizationOf);
        }
        else if (xapiContextActivities.getParent() != null) {
          XApiObject parent = xapiContextActivities.getParent().get(0);
          String type = null;
          String name = null;
          String description = null;
          XApiObjectDefinition xapiObjectDefinition = parent.getDefinition();
          if (xapiObjectDefinition != null) {
            type = xapiObjectDefinition.getType();
            
            Map<String,String> names =xapiObjectDefinition.getName();
            if (names != null) {
              if (names.size() == 1) {
                name = CollectionUtils.get(names, 0).getValue();
              }
              else {
                // default to en?
                name = names.get("en");
              }
            }

            Map<String,String> descriptions = xapiObjectDefinition.getDescription();
            if (descriptions != null) {
              if (descriptions.size() == 1) {
                description = CollectionUtils.get(descriptions, 0).getValue();
              }
              else {
                // default to en?
                description = descriptions.get("en");
              }
            }
          }

          caliperGroup = new Group(parent.getId(), 
              Context.CONTEXT.getValue(), 
              type, 
              name, 
              description, 
              contextExtensions, 
              null);
        }
      }
    }
    
    return new Event(statement.getId(),
        Context.CONTEXT.getValue(), 
        xapiToCaliperType(statement), 
        eventTime, 
        new DateTime(DateTimeZone.UTC),
        caliperActor, 
        caliperAction, 
        caliperObject, 
        null, // TODO map Target
        caliperGroup, 
        caliperResult); // TODO map Generated
  }
  
  public Statement toXapi(Event event) throws URISyntaxException {
    Statement statement = new Statement();
    
    // ID
    statement.setId(event.getId());
    // END ID
    
    // EVENT TIME
    DateTime eventTime = event.getEventTime();    
    if (eventTime != null) {
      DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
      statement.setTimestamp(fmt.print(eventTime));
    }
    // END EVENT TIME
    
    // ACTOR
    Actor actor = event.getActor();
    if (actor != null) {
      Map<String,String> actorExtensions = actor.getExtensions();
      XApiActor xapiActor = new XApiActor();
      
      String actorId = actor.getId();
      String actorType = actor.getType(); 
      xapiActor.setName(actor.getName());
      
      if (ACTOR_TYPE_OPENID.equals(actorType)) {
        xapiActor.setOpenid(actorId);
      }
      else if (ACTOR_TYPE_MBOX.equals(actorType)) {
        xapiActor.setMbox(actorId);
      }
      else {
        XApiAccount xapiAccount = new XApiAccount();
        
        if (actorExtensions != null && !actorExtensions.isEmpty()) {
          String homePage = actorExtensions.get("HOMEPAGE");
          if (StringUtils.isNotBlank(homePage)) {
            xapiAccount.setHomePage(homePage);
          }
        }
        
        xapiAccount.setName(actorId);
        xapiActor.setAccount(xapiAccount);
      }
      statement.setActor(xapiActor);
    }
    // ACTOR END
    
    // RESULT
    Generated caliperGeneratable = event.getGenerated();
    if (caliperGeneratable != null) {
      Map<String,String> caliperResultExtensions = caliperGeneratable.getExtensions();
      
      Map<URI,java.lang.Object> xapiResultExtensions = null;
      if (caliperResultExtensions != null && !caliperResultExtensions.isEmpty()) {
        xapiResultExtensions = new HashMap<>();
        for (Map.Entry<String,String> entry : caliperResultExtensions.entrySet()) {
          xapiResultExtensions.put(new URI(entry.getKey()), entry.getValue());
        }
        
        XApiResult xapiResult = new XApiResult();
        xapiResult.setExtensions(xapiResultExtensions);
        statement.setResult(xapiResult);
      }
    }
    // END Result


    // ACTION
    String action = event.getAction();
    if (StringUtils.isNotBlank(action)) {
      XApiVerb xapiVerb = new XApiVerb();
      xapiVerb.setId(action);
      statement.setVerb(xapiVerb);
    }
    // ACTION END
    
    // OBJECT
    Object object = event.getObject();
    if (object != null) {
      
      XApiObject xapiObject = new XApiObject();
      XApiObjectDefinition xapiObjectDefinition = new XApiObjectDefinition();
      
      String name = object.getName();
      if (StringUtils.isNotBlank(name)) {
        xapiObjectDefinition.setName(Collections.singletonMap("en", name));
      }
      
      String description = object.getDescription();
      if (StringUtils.isNotBlank(description)) {
        xapiObjectDefinition.setDescription(Collections.singletonMap("en", description));
      }
      
      xapiObjectDefinition.setType(caliperEntityTypeToXapiObjectType(object.getType()));
      
      Map<String,String> extensions = object.getExtensions();
      if (extensions != null && !extensions.isEmpty()) {
        Map<URI, java.lang.Object> xapiExtensions = new HashMap<>();
        for (String key : extensions.keySet()) {
          xapiExtensions.put(new URI(key), extensions.get(key));
        }
        xapiObjectDefinition.setExtensions(xapiExtensions);
      }
      
      xapiObject.setDefinition(xapiObjectDefinition);
      xapiObject.setId(object.getId());
      statement.setObject(xapiObject);
    }
    // OBJECT END
    
    Group group = event.getGroup();
    if (group != null) {
      XApiContext xapiContext = new XApiContext();
      
      Map<String,String> contextExtensions = group.getExtensions();
      if (contextExtensions != null && !contextExtensions.isEmpty()) {
        Map<URI,java.lang.Object> extensions = new HashMap<>();
        for (String key : contextExtensions.keySet()) {
          extensions.put(new URI(key), extensions.get(key));
        }
        xapiContext.setExtensions(extensions);
      }
      
      XApiContextActivities xapiContextActivities = new XApiContextActivities();
      XApiObject grouping = new XApiObject();
      grouping.setId(group.getId());      
      xapiContextActivities.setGrouping(Collections.singletonList(grouping));
      
      xapiContext.setContextActivities(xapiContextActivities);
      statement.setContext(xapiContext);
      
    }

    return statement;
  }
  
  private String xApiVerbToCaliperAction(String xapiVerbId) {
    Action caliperAction = verbActionMap.getKey(xapiVerbId);
    if (caliperAction == null) {
      return xapiVerbId;
    }
    
    return caliperAction.getValue();
  }
  
  private String caliperActionToXapiVerb(String caliperAction) {
    String verb = null;
    try {
      verb = verbActionMap.get(Action.valueOf(caliperAction));
      if (StringUtils.isBlank(verb)) {
        return DEFAULT_XAPI_VERB;
      }

    }
    catch (IllegalArgumentException e) {
      return DEFAULT_XAPI_VERB;
    }
    
    return verb;
  }
  
  private String xapiObjectTypeToCaliperEntityType(String xapiType) {
    Type caliperType = objectEntityMap.getKey(xapiType);
    if (caliperType == null) {
      return xapiType;
    }
    
    return caliperType.getValue();
  }
  
  private String caliperEntityTypeToXapiObjectType(String caliperType) {
    String xapiType = objectEntityMap.get(caliperType);
    if (StringUtils.isBlank(xapiType)) {
      return caliperType;
    }
    
    return xapiType;
  }
  
  private String xapiToCaliperType(Statement statement) {
    // default to event
    String type = EventType.EVENT.getValue();
    // change if there is something more specific
    XApiVerb xapiVerb = statement.getVerb();
    if (xapiVerb != null) {
      String verbId = xapiVerb.getId();
      Action caliperAction = verbActionMap.getKey(verbId);
      EventType eventType = actionEventMap.get(caliperAction);
      if (eventType != null) {
        type = eventType.getValue();
      }
    }
    
    return type;
  }
  
  private static final String ACTOR_TYPE_MBOX = "foaf:mbox";
  private static final String ACTOR_TYPE_OPENID = "http://openid.net/";
  private static final String ACTOR_TYPE_ACCOUNT = "https://github.com/adlnet/xAPI-Spec/blob/master/xAPI.md#agentaccount";
  
  private static final String DEFAULT_XAPI_VERB = "http://adlnet.gov/expapi/verbs/experienced";
}
