/* 
 * Copyright (c) 2008-2010, Hazel Ltd. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.impl.ascii.memcache;

import com.hazelcast.impl.ascii.AbstractTextCommandProcessor;
import com.hazelcast.impl.ascii.TextCommandService;
import com.hazelcast.nio.IOUtil;

public class GetCommandProcessor extends AbstractTextCommandProcessor<GetCommand> {
    final boolean single;

    public GetCommandProcessor(TextCommandService textCommandService, boolean single) {
        super(textCommandService);
        this.single = single;
    }

    public void handle(GetCommand getCommand) {
        String key = getCommand.getKey();
        String mapName = "default";
        int index = key.indexOf(':');
        if (index != -1) {
            mapName = key.substring(0, index);
            key = key.substring(index + 1);
        }
        Object value = textCommandService.get(mapName, key);
        MemcacheEntry entry = null;
        if (value != null) {
            if (value instanceof MemcacheEntry) {
                entry = (MemcacheEntry) value;
            } else {
                try {
                    entry = new MemcacheEntry(key, IOUtil.serializeToBytes(value), 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        textCommandService.incrementGetCount();
        if (entry != null) {
            textCommandService.incrementHitCount();
        }
        getCommand.setValue(entry, single);
        textCommandService.sendResponse(getCommand);
    }

    public void handleRejection(GetCommand getCommand) {
        getCommand.setValue(null, single);
        textCommandService.sendResponse(getCommand);
    }
}
