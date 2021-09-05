ECHO = /bin/echo

BASE		:= $(IMPT_ROOT)
OPERATIONAL	:= $(BASE)/DOS
REGISTRY	:= localhost:5000

# Inhouse images for local registry
SSO_IMG        := $(REGISTRY)/lola/sso:latest

all:	sso_push

sso_push:	sso_build
	(docker push $(SSO_IMG))

sso_build:
	(cd $(OPERATIONAL)/sso && \
	docker image build -f Dockerfile . \
	-t "$(SSO_IMG)")
clean:
	- (docker rmi $(SSO_IMG))
