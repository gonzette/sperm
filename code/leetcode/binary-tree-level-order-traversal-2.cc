/**
 * Definition for binary tree
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode(int x) : val(x), left(NULL), right(NULL) {}
 * };
 */

#include <cstdio>
#include <vector>

using namespace std;
struct TreeNode {
  int val;
  TreeNode* left;
  TreeNode* right;
  TreeNode(int x) : val(x), left(NULL), right(NULL) {}
};

typedef vector< vector<int> > VVI;
typedef vector<int> VI;

class Solution {
public:
  vector<vector<int> > levelOrderBottom(TreeNode* root) {
    // Note: The Solution object is instantiated only once and is reused by each test case.
    VVI vvi;
    R(root, 0, vvi);
    VVI vvi2;
    for(int i = vvi.size() - 1; i >= 0; i--) {
      vvi2.push_back(vvi[i]);
    }
    return vvi2;
  }
  void R(TreeNode* root, int depth, VVI& vvi) {
    if(root == NULL) {
      return ;
    }
    if(depth == vvi.size()) {
      VI vi;
      vvi.push_back(vi);
    }
    R(root->left, depth + 1, vvi);
    R(root->right, depth + 1, vvi);
    VI& vi = vvi[depth];
    vi.push_back(root->val);
  }
};

int main() {
  return 0;
}
