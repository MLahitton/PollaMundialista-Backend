package com.mundialpolla.shared.time;

import java.time.Instant;

public interface ApplicationClock {

    Instant now();
}
