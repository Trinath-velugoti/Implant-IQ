import socket
import os

def get_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # doesn't even have to be reachable
        s.connect(('10.255.255.255', 1))
        IP = s.getsockname()[0]
    except Exception:
        IP = '127.0.0.1'
    finally:
        s.close()
    return IP

print("\n" + "="*50)
print("     IMPLANT IQ - CONNECTION DIAGNOSTIC")
print("="*50)
print(f"\n1. YOUR LAPTOP IP IS: {get_ip()}")
print(f"2. YOUR PORT IS: 8080")
print(f"\n3. TYPE THIS INTO YOUR MOBILE CHROME BROWSER:")
print(f"   http://{get_ip()}:8080/api/health")
print("\n" + "="*50)
print("If the browser shows 'ok', the connection is FIXED.")
print("If it still says 'Site can't be reached', your phone is NOT")
print("on the same network as your laptop or Firewall is still ON.")
print("="*50 + "\n")
