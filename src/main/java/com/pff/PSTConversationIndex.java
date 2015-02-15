package com.pff;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Class to hold decoded PidTagConversationIndex
 *
 * @author Nick Buller
 */
public class PSTConversationIndex {
	private static final int HUNDRED_NS_TO_MS = 1000;
	private static final int MINIMUM_HEADER_SIZE = 22;
	private static final int RESPONSE_LEVEL_SIZE = 5;

	private Date deliveryTime;
	private UUID guid;
	private List<ResponseLevel> responseLevels = new ArrayList<ResponseLevel>();

	public Date getDeliveryTime() {
		return deliveryTime;
	}

	public UUID getGuid() {
		return guid;
	}

	public List<ResponseLevel> getResponseLevels() {
		return responseLevels;
	}

	public String toString() {
		return guid + "@" + deliveryTime + " " + responseLevels.size() + " ResponseLevels";
	}

	public class ResponseLevel {
		short deltaCode;
		long timeDelta;
		short random;

		public ResponseLevel(short deltaCode, long timeDelta, short random) {
			this.deltaCode = deltaCode;
			this.timeDelta = timeDelta;
			this.random = random;
		}

		public short getDeltaCode() {
			return deltaCode;
		}

		public long getTimeDelta() {
			return timeDelta;
		}

		public short getRandom() {
			return random;
		}

		public Date withOffset(Date anchorDate) {
			return new Date(anchorDate.getTime() + timeDelta);
		}
	}

	protected PSTConversationIndex(byte[] rawConversationIndex) {
		if (rawConversationIndex != null && rawConversationIndex.length >= MINIMUM_HEADER_SIZE) {
			decodeHeader(rawConversationIndex);
			if (rawConversationIndex.length >= MINIMUM_HEADER_SIZE + RESPONSE_LEVEL_SIZE) {
				decodeResponseLevel(rawConversationIndex);
			}
		}
	}

	private void decodeHeader(byte[] rawConversationIndex) {
		// According to the Spec the first byte is not included, but I believe the spec is incorrect!
		//int reservedheaderMarker = (int) PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 0, 1);

		long deliveryTimeHigh = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 0, 4);
		long deliveryTimeLow = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 4, 6) << 16;
		deliveryTime = PSTObject.filetimeToDate((int) deliveryTimeHigh, (int) deliveryTimeLow);

		long guidHigh = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 6, 14);
		long guidLow = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, 14, 22);

		guid = new UUID(guidHigh, guidLow);
	}

	private void decodeResponseLevel(byte[] rawConversationIndex) {
		int responseLevelCount = (rawConversationIndex.length - MINIMUM_HEADER_SIZE) / RESPONSE_LEVEL_SIZE;
		responseLevels = new ArrayList<ResponseLevel>(responseLevelCount);

		for (int responseLevelIndex = 0, position = 22; responseLevelIndex < responseLevelCount; responseLevelIndex++, position += RESPONSE_LEVEL_SIZE) {

			long responseLevelValue = PSTObject.convertBigEndianBytesToLong(rawConversationIndex, position, position + 5);
			short deltaCode = (short) (responseLevelValue >> 39);
			short random = (short) (responseLevelValue & 0xFF);

			// shift by 1 byte (remove the random) and mask off the deltaCode
			long deltaTime = (responseLevelValue >> 8) & 0x7FFFFFFF;

			if (deltaCode == 0) {
				deltaTime <<= 18;
			} else {
				deltaTime <<= 23;
			}

			deltaTime /= HUNDRED_NS_TO_MS;

			responseLevels.add(responseLevelIndex, new ResponseLevel(deltaCode, deltaTime, random));
		}
	}
}
