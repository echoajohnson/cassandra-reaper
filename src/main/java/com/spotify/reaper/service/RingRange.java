/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.reaper.service;

import java.math.BigInteger;

public class RingRange {
  private final BigInteger start;
  private final BigInteger end;

  public RingRange(BigInteger start, BigInteger end) {
    this.start = start;
    this.end = end;
  }

  public BigInteger getStart() {
    return start;
  }

  public BigInteger getEnd() {
    return end;
  }

  public BigInteger span(BigInteger ringSize) {
    if (SegmentGenerator.greaterThanOrEqual(start, end)) {
      return end.subtract(start).add(ringSize);
    } else {
      return end.subtract(start);
    }
  }

  public boolean encloses(RingRange other) {
    // TODO: unit test for this
    if (SegmentGenerator.lowerThanOrEqual(start, end)) {
      return SegmentGenerator.greaterThanOrEqual(other.start, start) &&
             SegmentGenerator.lowerThanOrEqual(other.end, end);
    } else if (SegmentGenerator.lowerThanOrEqual(other.start, other.end)) {
      return SegmentGenerator.greaterThanOrEqual(other.start, start) ||
             SegmentGenerator.lowerThanOrEqual(other.end, end);
    } else {
      return SegmentGenerator.greaterThanOrEqual(other.start, start) &&
             SegmentGenerator.lowerThanOrEqual(other.end, end);
    }
  }

  @Override
  public String toString() {
    return String.format("(%s,%s]", start.toString(), end.toString());
  }
}
