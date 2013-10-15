#include <string>
#include <stack>
using namespace std;

class Solution {
public:
  bool isValid(string s) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    stack<char> st;
    for(int i = 0; i < s.size(); i++) {
      char ch = s[i];
      if(ch == '(' || ch == '{' || ch == '[') {
        st.push(ch);
      } else if(ch == ')' || ch == '}' || ch == ']') {
        if(st.empty()) {
          return false;
        }
        char x = st.top();
        if(!((ch == ')' && x == '(') ||
             (ch == '}' && x == '{') ||
             (ch == ']' && x == '['))) {
          return false;
        }
        st.pop();
      }
    }
    return st.empty();
  }
};

int main() {
  return 0;
}
