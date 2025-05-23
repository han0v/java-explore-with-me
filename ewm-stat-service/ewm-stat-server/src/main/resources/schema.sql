CREATE TABLE IF NOT EXISTS endpoint_hits (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app VARCHAR(255) NOT NULL,
    uri VARCHAR(512) NOT NULL,
    ip VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_endpoint_hits_timestamp ON endpoint_hits (timestamp);
CREATE INDEX IF NOT EXISTS idx_endpoint_hits_uri ON endpoint_hits (uri);