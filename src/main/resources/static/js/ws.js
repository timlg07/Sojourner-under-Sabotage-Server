class EventSystem {

  constructor() {
    this.handlers = []; // TODO: make it a map from event type to func
    this.stompClient = this._initStompClient();
    this.stompClient.activate();
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
    this.handlers.forEach(h => h(event));
  }

  registerHandler(eventHandler) {
    this.handlers.push(eventHandler);
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

    this.stompClient.publish({
      destination: '/app/events',
      body: JSON.stringify(event),
      skipContentLengthHeader: true,
    });
  }
}

class SusEvent {
  timestamp;
  type;
}

class RoomUnlockedEvent extends SusEvent {
  type = ".RoomUnlockedEvent";
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
  type = ".ComponentTestsActivatedEvent";
  /** @type {String} */
  componentName;

  constructor(componentName) {
    super();
    this.componentName = componentName;
  }
}
