#include <vector>
using namespace std;

class Solution {
public:
  int canCompleteCircuit(vector<int>& gas, vector<int>& cost) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    const int n = gas.size();
    int g = 0;
    int c = 0;
    for(int i = 0; i < n; i++) {
      g += gas[i];
      c += cost[i];
    }
    if(g < c) {
      return -1;
    }

    // eliminate subsequence where sum of them <0.
    int id = 0;
    int sv = id;
    g = 0;
    c = 0;
    for(;;) {
      g += gas[id];
      c += cost[id];
      id = (id + 1) % n;
      if(g < c) {
        g = 0;
        c = 0;
        sv = id;
      }
      if(id == 0) {
        break;
      }
    }
    return sv;
  }
};

int main() {
  return 0;
}
