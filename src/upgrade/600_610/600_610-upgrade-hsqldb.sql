-- These columns are added by the JPA provider if there are sufficient privileges
-- ALTER TABLE KeyRecoveryData ADD cryptoTokenId INTEGER DEFAULT 0 NOT NULL;
-- ALTER TABLE KeyRecoveryData ADD keyAlias VARCHAR(256) DEFAULT NULL;
-- ALTER TABLE KeyRecoveryData ADD publicKeyId VARCHAR(256) DEFAULT NULL;
-- If there were existing data in the table, we set the value of cryptoTokenId to 0, even if it's default 0
-- UPDATE KeyRecoveryData SET cryptoTokenId=0;
