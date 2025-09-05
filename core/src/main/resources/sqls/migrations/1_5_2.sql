UPDATE `setting` SET `type` = 'CHAT_NAME' WHERE `type` = 'CHAT';

UPDATE `setting` SET `type` = 'SPY_STATUS' WHERE `type` = 'SPY';

DELETE FROM `setting` WHERE `value` = '';