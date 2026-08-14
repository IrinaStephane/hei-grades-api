package school.hei.api.endpoint.event.consumer.model;

import school.hei.api.PojaGenerated;
import school.hei.api.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
