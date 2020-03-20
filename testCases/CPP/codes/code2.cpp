#include <iostream> 
#include <bits/stdc++.h> 
using namespace std; 
int main() 
{ 
    char a[] = "Hello World"; 
    char* p = a; 
    printf("%d %d %d", sizeof(a), sizeof(p), strlen(a)); 
    return 0; 
} 
