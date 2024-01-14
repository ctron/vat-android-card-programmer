# Configuration

## Encrypt file

```shell
cat example1.json | openssl aes-256-cbc -pbkdf2 -iter 65536 -a -k "foobar" > example1.json.txt
```