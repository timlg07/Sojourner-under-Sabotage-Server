class EventSystem {

  constructor() {
    /** @type {Map<String, Array<Function>>} */
    this.handlers = new Map();
    this.stompClient = this._initStompClient();
    this.stompClient.activate();
    this.queue = [];
  }

  _initStompClient() {
    if (typeof StompJs === 'undefined') {
      throw new Error('StompJs is not available');
    }

    const eventSystemInstance = this;
    const wsUrl = `ws://${window.location.host}/websocket`;
    const client = new StompJs.Client({
      brokerURL: wsUrl,
      connectHeaders: window.csrfHeader,
      debug: function (str) {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = function (frame) {
      // Do something, all subscribes must be done is this callback
      // This is needed because this will be executed after a (re)connect
      console.log("StompJs connected to broker over ws");
      client.subscribe('/user/queue/events', (message) => {
        const event = JSON.parse(message.body);
        eventSystemInstance._handleEvent(event);
      });

      while (eventSystemInstance.queue.length > 0) {
        const event = eventSystemInstance.queue.shift();
        eventSystemInstance.sendEvent(event);
      }
    };

    client.onStompError = function (frame) {
      // Will be invoked in case of error encountered at Broker
      // Bad login/passcode typically will cause an error
      // Complaint brokers will set `message` header with a brief message.
      // The Body may contain details.
      // Compliant brokers will terminate the connection after any error
      console.log('Broker reported error: ' + frame.headers['message']);
      console.log('Additional details: ' + frame.body);
    };

    return client;
  }

  _handleEvent(event) {
    const eventType = event.type.replace(/^(\.)/, '');
    if (this.handlers.has(eventType)) {
      this.handlers.get(eventType).forEach((handler) => handler(event));
    }
    if (this.handlers.has('*')) {
      this.handlers.get('*').forEach((handler) => handler(event));
    }
  }

  /**
   * Register a handler for a specific event type
   * @param {String} eventType the name of the event type or * for all events.
   *                           Can contain a leading dot that will be removed.
   * @param {Function} eventHandler the handler function
   */
  registerHandler(eventType, eventHandler) {
    eventType = eventType.replace(/^(\.)/, '');
    if (this.handlers.has(eventType)) {
      this.handlers.get(eventType).push(eventHandler);
    } else {
      this.handlers.set(eventType, [eventHandler]);
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
    console.log("Disconnected");
  }

  /**
   * Send an event to the server
   *
   * @param {SusEvent} event the event to send
   */
  sendEvent(event) {
    if (!event) event = {};
    if (!event.timestamp) event.timestamp = Date.now();

    if (this.stompClient.connected) {
      this.stompClient.publish({
        destination: '/app/events',
        body: JSON.stringify(event),
        skipContentLengthHeader: true,
      });
    } else {
      this.queue.push(event);
    }
  }
}

class SusEvent {
  timestamp;
  type;
}

class RoomUnlockedEvent extends SusEvent {
  static type = ".RoomUnlockedEvent";
  type = RoomUnlockedEvent.type;
  /** @type {Number} */
  roomId;

  /**
   * @param {Number} roomId the room id
   */
  constructor(roomId) {
    super();
    this.roomId = roomId;
  }
}

class ComponentTestsActivatedEvent extends SusEvent {
  static type = ".ComponentTestsActivatedEvent";
  type = ComponentTestsActivatedEvent.type;
  /** @type {String} */
  componentName;

  constructor(componentName) {
    super();
    this.componentName = componentName;
  }
}

class GameStartedEvent extends SusEvent {
  static type = ".GameStartedEvent";
  type = GameStartedEvent.type;
}
