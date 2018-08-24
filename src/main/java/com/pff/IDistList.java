package com.pff;

import java.io.IOException;

public interface IDistList extends IMessage {
    /**
     * Get an array of the members in this distribution list.
     *
     * @throws PSTException
     *             on corrupted data
     * @throws IOException
     *             on bad string reading
     * @return array of entries that can either be PSTDistList.OneOffEntry
     *         or a PSTObject, generally PSTContact.
     */
    Object[] getDistributionListMembers() throws PSTException, IOException;
}
