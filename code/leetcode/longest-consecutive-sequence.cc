#include <vector>
#include <algorithm>

using namespace std;

class Solution {
public:
  int longestConsecutive(vector<int>& num) {
    // Start typing your C/C++ solution below
    // DO NOT write int main() function
    if(num.size() == 0 || num.size() == 1) {
      return num.size();
    }
    sort(num.begin(), num.end());
    int length = 1;
    int code = 1;
    for(int i = 1; i < num.size(); i++) {
      if(num[i] == num[i - 1]) {
        continue;
      }
      // consecutive.
      if((num[i] - num[i - 1]) == 1) {
        length++;
      } else {
        // update it.
        if(length > code) {
          code = length;
        }
        length = 1;
      }
    }
    // update at last.
    if(length > code) {
      code = length;
    }
    return code;
  }
};

int main() {
  return 0;
}
