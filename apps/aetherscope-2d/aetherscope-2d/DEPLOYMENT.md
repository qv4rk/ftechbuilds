# Deployment Guide

## Quick Deployment

Build the project:
```
npm run build
```

Copy to server:
```
scp -r dist/* qv4rk@172.234.194.44:/var/www/main/apps/kaleidoscope/
```

Or via Git:
```
cd ~/ftechbuilds
mkdir -p apps/kaleidoscope
cp -r ~/aetherscope-2d/dist/* apps/kaleidoscope/
git add apps/kaleidoscope/
git commit -m "Deploy AetherScope 2D"
git push origin main
```

Access at: https://feisttech.com/apps/kaleidoscope/

See full documentation in README.md
