/* coding:utf-8
 * Copyright (C) dirlt
 */

#include "common/lkfree.h"
typedef common::lkfree::SampleMemoryAllcator<int> IntSampleMemoryAllocator;

int main() {
  IntSampleMemoryAllocator ma;
  IntSampleMemoryAllocator::Entry* e = ma.alloc();
  ma.free(e);
  return 0;
}
