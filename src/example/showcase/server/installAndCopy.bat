call mvn clean install

call rd ..\..\..\..\dist\examples\showcase\server /s /q
call xcopy target\dist\tio-examples-showcase-server-1.7.1.v20170604-RELEASE ..\..\..\..\dist\examples\showcase\server\ /s /e /q /y

