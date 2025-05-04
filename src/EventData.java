public class EventData<T> {
    private Event eventType;
    private T data;
    public EventData(Event eventType, T data) {
        this.eventType = eventType;
        this.data = data;
    }

    public Event getEventType() {
        return eventType;
    }
    public T getData() {
        return data;
    }
}
