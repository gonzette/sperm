#include <cstdio>
#include <vector>
using namespace std;

class Solution {
public:
  vector<int> grayCode(int n) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    vector<int> rs;
    int v = 0;
    rs.push_back(v);
    for(int i = 1; i < (1 << n) ; i++) {
      int c = 0;
      int idx = i;
      while(!(idx & 0x1)) {
        c++;
        idx >>= 1;
      }
      printf("%d\n", c);
      v ^= (1 << c);
      rs.push_back(v);
    }
    return rs;
  }
};

int main() {
  Solution s;
  s.grayCode(2);
  return 0;
}
